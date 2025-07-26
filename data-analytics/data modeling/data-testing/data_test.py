import joblib
import pandas as pd
import numpy as np
import warnings
warnings.filterwarnings('ignore')

def load_training_coordinates():
    """Extract all unique coordinate combinations from training data"""
    print("Reading coordinate combinations from training data...")
    
    try:
        # Read training data
        df = pd.read_csv("final_dataset.csv")
        print(f"✓ Successfully loaded training data, {len(df)} rows in total")
        
        # Extract unique coordinate combinations
        coordinates = df[['Latitude', 'Longitude']].drop_duplicates().reset_index(drop=True)
        print(f"✓ Found {len(coordinates)} unique coordinate combinations")
        
        return coordinates
        
    except Exception as e:
        print(f"✗ Failed to read training data: {e}")
        return None

def main():
    print("Starting to load XGBoost model...")
    
    # Load model
    try:
        model = joblib.load("xgboost_model.pkl")
        print("✓ Model loaded successfully")
    except Exception as e:
        print(f"✗ Model loading failed: {e}")
        return
    
    # Load coordinate combinations from training data
    coordinates = load_training_coordinates()
    if coordinates is None:
        return
    
    # Cultural activity categories
    activities = ['Art Sale', 'Busking', 'Filmmaking', 'Portrait photography', 
                 'Street photography', 'Landscape painting', 'Portrait painting']
    
    # Store all top 5 results
    all_top_5_results = []
    
    print("Starting 100 test rounds, each round will test all coordinate combinations...")
    
    for test_round in range(1, 101):  # 100 test rounds
        print(f"Starting test round {test_round}...")
        
        # Randomly select same time and activity type for each test
        np.random.seed(test_round)  # Use different seed for each round
        
        # Generate unified random time and activity for this round
        test_hour = np.random.randint(0, 24)
        test_month = np.random.randint(1, 13)
        test_day = np.random.randint(1, 29)
        test_activity = np.random.choice(activities)
        
        print(f"  Test conditions: {test_hour}:00, {test_month}/{test_day}, Activity: {test_activity}")
        
        # Create test data: iterate through all coordinates using same time and activity
        test_data = coordinates.copy()
        test_data['Hour'] = test_hour
        test_data['Month'] = test_month
        test_data['Day'] = test_day
        test_data['Cultural_activity_prefered'] = test_activity
        
        print(f"  Making predictions for {len(test_data)} locations...")
        
        # Make predictions
        try:
            predictions = model.predict(test_data)
        except Exception as e:
            print(f"✗ Prediction failed for round {test_round}: {e}")
            continue
        
        # Merge results
        results = test_data.copy()
        results['Total_Crowd'] = predictions[:, 0]
        results['Taxi_Zone_Score'] = predictions[:, 1] 
        results['Activity_Score'] = predictions[:, 2]
        
        # Find top 5 for this round (based on Total_Crowd)
        top_5_this_round = results.nlargest(5, 'Total_Crowd')
        
        # Add test round information
        top_5_this_round['Test_Round'] = test_round
        top_5_this_round['Rank_in_Round'] = range(1, 6)
        
        # Add to total results
        all_top_5_results.append(top_5_this_round)
        
        print(f"  ✓ Test round {test_round} completed, highest crowd count: {top_5_this_round.iloc[0]['Total_Crowd']:.0f}")
    
    # Merge all results
    final_results = pd.concat(all_top_5_results, ignore_index=True)
    
    print(f"\n✓ 100 test rounds completed, obtained {len(final_results)} rows of results")
    
    # Reorder columns
    column_order = ['Test_Round', 'Rank_in_Round', 'Latitude', 'Longitude', 
                   'Hour', 'Month', 'Day', 'Cultural_activity_prefered',
                   'Taxi_Zone_Score', 'Total_Crowd', 'Activity_Score']
    final_results = final_results[column_order]
    
    # Display overall top 10
    overall_top_10 = final_results.nlargest(10, 'Total_Crowd')
    
    print("\n" + "="*80)
    print("Top 10 highest crowd counts across all tests:")
    print("="*80)
    
    for i, (_, row) in enumerate(overall_top_10.iterrows(), 1):
        print(f"\nOverall Rank {i} (from test round {row['Test_Round']}):")
        print(f"Coordinates: ({row['Latitude']:.4f}, {row['Longitude']:.4f})")
        print(f"Time: {row['Hour']:02d}:00")
        print(f"Date: 2024/{row['Month']:02d}/{row['Day']:02d}")
        print(f"Cultural Activity: {row['Cultural_activity_prefered']}")
        print(f"Taxi Zone Score: {row['Taxi_Zone_Score']:.3f}")
        print(f"Total Crowd: {row['Total_Crowd']:.0f}")
        print(f"Activity Score: {row['Activity_Score']:.3f}")
        print("-" * 50)
    
    # Save complete results
    filename = 'improved_top_5_crowd_locations_100_tests.csv'
    final_results.to_csv(filename, index=False)
    print(f"\n✓ Complete results saved to file: {filename}")
    
    # Output statistics
    print(f"\nStatistics:")
    print(f"Average highest crowd count: {final_results[final_results['Rank_in_Round']==1]['Total_Crowd'].mean():.0f}")
    print(f"Maximum crowd count: {final_results['Total_Crowd'].max():.0f}")
    print(f"Minimum crowd count: {final_results['Total_Crowd'].min():.0f}")
    
    # Activity type distribution
    print(f"\nOccurrence count of each activity type in top 5:")
    activity_counts = final_results['Cultural_activity_prefered'].value_counts()
    for activity, count in activity_counts.items():
        print(f"{activity}: {count} times")

if __name__ == "__main__":
    main()