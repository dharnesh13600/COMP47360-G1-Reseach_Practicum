import pandas as pd
import uuid
import os
from datetime import datetime

class CreativeSpaceFinderMLReadyImporter:
    def __init__(self):
        self.df = None
        self.activities_map = {}
        self.taxi_zones_map = {}
        self.event_locations_map = {}
        self.activity_names = [
            'Portrait photography',
            'Street photography',
            'Landscape painting',
            'Portrait painting',
            'Art Sale',
            'Busking',
            'Filmmaking'
        ]

    def load_data(self):
        print("Loading CSV data...")
        base_path = os.path.dirname(os.path.abspath(__file__))
        csv_path = os.path.join(base_path, 'final_dataset.csv')
        self.df = pd.read_csv(csv_path)

        self.df = self.df.dropna(subset=[
            'Cultural_activity_prefered',
            'Nearest Taxi Zone',
            'Event Location',
            'Date',
            'Time'
        ])
        print(f"Data cleaned, {len(self.df)} records remaining")

    def generate_activities_sql(self):
        print("Generating activities SQL...")
        unique_activities = self.df['Cultural_activity_prefered'].unique()
        sql_statements = ["""
-- Activities Table (Simplified)
CREATE TABLE IF NOT EXISTS activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);
"""]

        for activity in unique_activities:
            if activity in self.activity_names:
                activity_id = str(uuid.uuid4())
                self.activities_map[activity] = activity_id
                sql_statements.append(f"""
INSERT INTO activities (id, name) 
VALUES ('{activity_id}', '{activity}');
""")
        return '\n'.join(sql_statements)

    def generate_taxi_zones_sql(self):
        print("Generating taxi zones SQL...")
        unique_zones = self.df.groupby('Nearest Taxi Zone').agg({
            'Zone Latitude': 'first',
            'Zone Longitude': 'first'
        }).reset_index()

        sql_statements = ["""
-- Taxi Zones Table
CREATE TABLE IF NOT EXISTS taxi_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    zone_name VARCHAR(100) NOT NULL UNIQUE,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_taxi_zones_coordinates ON taxi_zones (latitude, longitude);
"""]

        for _, row in unique_zones.iterrows():
            zone_id = str(uuid.uuid4())
            self.taxi_zones_map[row['Nearest Taxi Zone']] = zone_id
            sql_statements.append(f"""
INSERT INTO taxi_zones (id, zone_name, latitude, longitude) 
VALUES ('{zone_id}', '{row['Nearest Taxi Zone'].replace("'", "''")}', {row['Zone Latitude']}, {row['Zone Longitude']});
""")
        return '\n'.join(sql_statements)

    def generate_event_locations_sql(self):
        print("Generating event locations SQL...")
        unique_locations = self.df.groupby('Event Location').agg({
            'Latitude': 'first',
            'Longitude': 'first',
            'Nearest Taxi Zone': 'first'
        }).reset_index()

        sql_statements = ["""
-- Event Locations Table
CREATE TABLE IF NOT EXISTS event_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    location_name TEXT NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    nearest_taxi_zone_id UUID REFERENCES taxi_zones(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_event_locations_coordinates ON event_locations (latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_event_locations_name ON event_locations (location_name);
CREATE INDEX IF NOT EXISTS idx_event_locations_taxi_zone ON event_locations (nearest_taxi_zone_id);
"""]

        for _, row in unique_locations.iterrows():
            location_id = str(uuid.uuid4())
            self.event_locations_map[row['Event Location']] = location_id
            taxi_zone_id = self.taxi_zones_map.get(row['Nearest Taxi Zone'])
            sql_statements.append(f"""
INSERT INTO event_locations (id, location_name, latitude, longitude, nearest_taxi_zone_id) 
VALUES ('{location_id}', '{row['Event Location'].replace("'", "''")}', {row['Latitude']}, {row['Longitude']}, '{taxi_zone_id}');
""")
        return '\n'.join(sql_statements)

    def generate_location_activity_scores_sql(self):
        print("Generating location activity scores SQL...")
        sql_statements = ["""
-- Location Activity Scores Table (ML-Ready)
CREATE TABLE IF NOT EXISTS location_activity_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id INTEGER NOT NULL,
    location_id UUID NOT NULL REFERENCES event_locations(id),
    activity_id UUID NOT NULL REFERENCES activities(id),
    taxi_zone_id UUID NOT NULL REFERENCES taxi_zones(id),
    event_date DATE NOT NULL,
    event_time TIME NOT NULL,
    historical_taxi_zone_crowd_score DECIMAL(5, 3),
    historical_activity_score DECIMAL(5, 2),
    cultural_activity_score DECIMAL(5, 2),
    crowd_score DECIMAL(5, 2),
    muse_score DECIMAL(5, 2),
    ml_prediction_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_location_activity_scores_location ON location_activity_scores (location_id);
CREATE INDEX IF NOT EXISTS idx_location_activity_scores_activity ON location_activity_scores (activity_id);
CREATE INDEX IF NOT EXISTS idx_location_activity_scores_datetime ON location_activity_scores (event_date, event_time);
CREATE INDEX IF NOT EXISTS idx_location_activity_scores_composite ON location_activity_scores (activity_id, event_date, event_time);
CREATE INDEX IF NOT EXISTS idx_location_activity_scores_muse ON location_activity_scores (muse_score) WHERE muse_score IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_location_activity_scores_ml_ready ON location_activity_scores (cultural_activity_score, crowd_score) WHERE cultural_activity_score IS NOT NULL AND crowd_score IS NOT NULL;
"""]

        batch_size = 1000
        total_batches = len(self.df) // batch_size + 1

        for batch in range(total_batches):
            batch_df = self.df.iloc[batch * batch_size:(batch + 1) * batch_size]
            print(f"Processed batch {batch + 1}/{total_batches}")
            for _, row in batch_df.iterrows():
                activity_id = self.activities_map.get(row['Cultural_activity_prefered'])
                location_id = self.event_locations_map.get(row['Event Location'])
                taxi_zone_id = self.taxi_zones_map.get(row['Nearest Taxi Zone'])
                if not (activity_id and location_id and taxi_zone_id):
                    continue
                try:
                    event_date = pd.to_datetime(row['Date']).strftime('%Y-%m-%d')
                    event_time = pd.to_datetime(row['Time'], format='%H:%M:%S').strftime('%H:%M:%S')
                except:
                    continue
                crowd_score = row.get('Taxi zone crowd score', 'NULL')
                act_score = row.get('Activity Score', 'NULL')
                crowd_score = 'NULL' if pd.isna(crowd_score) else crowd_score
                act_score = 'NULL' if pd.isna(act_score) else act_score
                score_id = str(uuid.uuid4())
                sql_statements.append(f"""
INSERT INTO location_activity_scores (
    id, event_id, location_id, activity_id, taxi_zone_id, event_date, event_time,
    historical_taxi_zone_crowd_score, historical_activity_score,
    cultural_activity_score, crowd_score, muse_score, ml_prediction_date
) VALUES (
    '{score_id}', {row['Event ID']}, '{location_id}', '{activity_id}', '{taxi_zone_id}', 
    '{event_date}', '{event_time}', {crowd_score}, {act_score},
    NULL, NULL, NULL, NULL
);
""")
        return '\n'.join(sql_statements)

    def generate_ml_support_tables_sql(self):
        return """
-- Weather Cache Table
CREATE TABLE IF NOT EXISTS weather_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    forecast_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    temperature DECIMAL(5, 2) NOT NULL,
    weather_condition VARCHAR(50) NOT NULL,
    weather_description VARCHAR(100) NOT NULL,
    cached_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_weather_cache_datetime ON weather_cache (forecast_datetime);
CREATE INDEX IF NOT EXISTS idx_weather_cache_expires ON weather_cache (expires_at);

-- ML Prediction Logs Table
CREATE TABLE IF NOT EXISTS ml_prediction_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_version VARCHAR(50) NOT NULL,
    prediction_type VARCHAR(50) NOT NULL,
    records_processed INTEGER NOT NULL,
    records_updated INTEGER NOT NULL,
    prediction_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    model_accuracy DECIMAL(5, 4),
    notes TEXT
);
CREATE INDEX IF NOT EXISTS idx_ml_logs_date_type ON ml_prediction_logs (prediction_date, prediction_type);
"""

    def generate_ml_helper_functions_sql(self):
        return """
-- Function to update ML predictions
CREATE OR REPLACE FUNCTION update_ml_predictions(
    p_location_activity_score_id UUID,
    p_cultural_activity_score DECIMAL(5, 2),
    p_crowd_score DECIMAL(5, 2),
    p_muse_score DECIMAL(5, 2)
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE location_activity_scores 
    SET 
        cultural_activity_score = p_cultural_activity_score,
        crowd_score = p_crowd_score,
        muse_score = p_muse_score,
        ml_prediction_date = NOW(),
        updated_at = NOW()
    WHERE id = p_location_activity_score_id;
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;
"""

    def export_sql(self, filename):
        print(f"Exporting SQL to {filename}...")
        sql = [
            "-- Creative Space Finder Database Import Script (ML-Ready)",
            "-- Generated on: " + datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";",
            self.generate_activities_sql(),
            self.generate_taxi_zones_sql(),
            self.generate_event_locations_sql(),
            self.generate_location_activity_scores_sql(),
            self.generate_ml_support_tables_sql(),
            self.generate_ml_helper_functions_sql()
        ]
        with open(filename, 'w', encoding='utf-8') as f:
            f.write('\n'.join(sql))
        print(f"SQL script exported to: {filename}")

    def print_summary(self):
        print("\n=== ML-READY IMPORT SUMMARY ===")
        print(f"Total records: {len(self.df)}")
        print(f"Unique activities: {len(self.activities_map)}")
        print(f"Unique taxi zones: {len(self.taxi_zones_map)}")
        print(f"Unique event locations: {len(self.event_locations_map)}")
        dates = pd.to_datetime(self.df['Date'])
        print(f"Date range: {dates.min().strftime('%m/%d/%Y')} to {dates.max().strftime('%m/%d/%Y')}")
        print("Activity distribution:")
        activity_counts = self.df['Cultural_activity_prefered'].value_counts()
        for activity, count in activity_counts.items():
            print(f"  {activity}: {count}")

# Run script
if __name__ == "__main__":
    importer = CreativeSpaceFinderMLReadyImporter()
    importer.load_data()
    base_dir = os.path.dirname(os.path.abspath(__file__))
    output_path = os.path.join(base_dir, 'final_supabase_import.sql')
    importer.export_sql(output_path)
    importer.print_summary()
