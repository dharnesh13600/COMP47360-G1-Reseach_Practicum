import pandas as pd
import numpy as np
from geopy.distance import geodesic
import random

def spatial_sampling(df, target_count=100, min_distance=100):
    """
    Spatial sampling: Select evenly distributed locations
    
    Parameters:
    - df: DataFrame containing latitude and longitude
    - target_count: Target number of locations to keep
    - min_distance: Minimum distance interval (meters)
    """
    
    # Get all unique coordinate combinations
    unique_coords = df[['Latitude', 'Longitude']].drop_duplicates().reset_index(drop=True)
    print(f"Original number of locations: {len(unique_coords)}")
    
    # If location count is already less than or equal to target, return directly
    if len(unique_coords) <= target_count:
        print(f"Location count is already less than or equal to {target_count}, no filtering needed")
        return df
    
    selected_points = []
    remaining_points = list(range(len(unique_coords)))
    
    # Randomly select first point
    first_idx = random.choice(remaining_points)
    selected_points.append(first_idx)
    remaining_points.remove(first_idx)
    
    print(f"Starting spatial sampling, target to keep {target_count} locations...")
    
    while len(selected_points) < target_count and remaining_points:
        best_candidate = None
        max_min_distance = 0
        
        # For each remaining point, calculate its minimum distance to selected points
        for candidate_idx in remaining_points:
            candidate_lat = unique_coords.loc[candidate_idx, 'Latitude']
            candidate_lon = unique_coords.loc[candidate_idx, 'Longitude']
            
            # Calculate minimum distance to all selected points
            min_dist_to_selected = float('inf')
            for selected_idx in selected_points:
                selected_lat = unique_coords.loc[selected_idx, 'Latitude']
                selected_lon = unique_coords.loc[selected_idx, 'Longitude']
                
                dist = geodesic((candidate_lat, candidate_lon), 
                              (selected_lat, selected_lon)).meters
                min_dist_to_selected = min(min_dist_to_selected, dist)
            
            # Select candidate point with maximum distance to existing points
            if min_dist_to_selected > max_min_distance:
                max_min_distance = min_dist_to_selected
                best_candidate = candidate_idx
        
        if best_candidate is not None:
            selected_points.append(best_candidate)
            remaining_points.remove(best_candidate)
            
            if len(selected_points) % 10 == 0:
                print(f"Selected {len(selected_points)} locations...")
    
    # Get selected coordinates
    selected_coords = unique_coords.loc[selected_points].reset_index(drop=True)
    print(f"Finally selected {len(selected_coords)} locations")
    
    # Filter original data to keep only selected locations
    filtered_df = df.merge(selected_coords, on=['Latitude', 'Longitude'], how='inner')
    
    print(f"Filtered data rows: {len(filtered_df)} (Original: {len(df)})")
    
    return filtered_df, selected_coords

def analyze_distribution(coords):
    """Analyze location distribution"""
    print("\nLocation Distribution Analysis:")
    print("="*40)
    
    distances = []
    for i in range(len(coords)):
        for j in range(i+1, len(coords)):
            lat1, lon1 = coords.iloc[i]['Latitude'], coords.iloc[i]['Longitude']
            lat2, lon2 = coords.iloc[j]['Latitude'], coords.iloc[j]['Longitude']
            dist = geodesic((lat1, lon1), (lat2, lon2)).meters
            distances.append(dist)
    
    distances = np.array(distances)
    print(f"Distance Statistics:")
    print(f"  Minimum distance: {distances.min():.0f} meters")
    print(f"  Maximum distance: {distances.max():.0f} meters")
    print(f"  Average distance: {distances.mean():.0f} meters")
    print(f"  Median distance: {np.median(distances):.0f} meters")
    
    # Count location pairs within different distance ranges
    close_pairs = np.sum(distances < 100)
    medium_pairs = np.sum((distances >= 100) & (distances < 500))
    far_pairs = np.sum(distances >= 500)
    
    print(f"\nDistance Distribution:")
    print(f"  Pairs <100m: {close_pairs}")
    print(f"  Pairs 100-500m: {medium_pairs}")
    print(f"  Pairs >500m: {far_pairs}")

# Main program
def main():
    print("Starting data processing...")
    
    # Read data
    df = pd.read_csv("final_dataset.csv")
    print(f"Original data: {len(df)} rows")
    
    # Perform spatial sampling
    filtered_df, selected_coords = spatial_sampling(df, target_count=100, min_distance=100)
    
    # Analyze distribution
    analyze_distribution(selected_coords)
    
    # Save results
    filtered_df.to_csv("final_dataset_100_locations.csv", index=False)
    selected_coords.to_csv("selected_100_coordinates.csv", index=False)
    
    print(f"\n✓ Saved filtered data to 'final_dataset_100_locations.csv'")
    print(f"✓ Saved selected coordinates to 'selected_100_coordinates.csv'")
    
    print(f"\nData filtering completed!")
    print(f"Original locations: 902 → Filtered locations: {len(selected_coords)}")
    print(f"Original rows: {len(df)} → Filtered rows: {len(filtered_df)}")

if __name__ == "__main__":
    main()