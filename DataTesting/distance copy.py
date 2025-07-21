import pandas as pd
import math
from collections import defaultdict
from geopy.distance import geodesic

def calculate_distance_geopy(lat1, lon1, lat2, lon2):
    """
    Calculate distance between two latitude/longitude points using GeoPy
    Returns distance in meters
    """
    point1 = (lat1, lon1)
    point2 = (lat2, lon2)
    return geodesic(point1, point2).meters

def find_connected_groups(distances, threshold=200):
    """
    Find groups of locations connected within threshold distance
    Using connected components concept from graph theory
    """
    n = len(distances)
    visited = [False] * n
    groups = []
    
    def dfs(node, current_group):
        visited[node] = True
        current_group.append(node)
        
        for neighbor in range(n):
            if not visited[neighbor] and distances[node][neighbor] <= threshold:
                dfs(neighbor, current_group)
    
    for i in range(n):
        if not visited[i]:
            current_group = []
            dfs(i, current_group)
            groups.append(current_group)
    
    return groups

def analyze_location_clusters_geopy():
    """
    Analyze distance relationships between 5 locations in each test round using GeoPy
    """
    print("Analyzing location distance relationships using GeoPy...")
    
    # Read data
    df = pd.read_csv("improved_top_5_crowd_locations_100_tests.csv")
    print(f"✓ Data loaded, {len(df)} rows in total")
    
    results = []
    
    # Group by test round
    for test_round in range(1, 101):
        round_data = df[df['Test_Round'] == test_round].copy()
        
        if len(round_data) != 5:
            print(f"⚠️ Test round {test_round} has incomplete data, skipping")
            continue
        
        # Extract coordinates
        locations = []
        for _, row in round_data.iterrows():
            locations.append((row['Latitude'], row['Longitude']))
        
        # Calculate distance matrix between all locations
        n = len(locations)
        distance_matrix = [[0] * n for _ in range(n)]
        
        for i in range(n):
            for j in range(i+1, n):
                dist = calculate_distance_geopy(
                    locations[i][0], locations[i][1],
                    locations[j][0], locations[j][1]
                )
                distance_matrix[i][j] = dist
                distance_matrix[j][i] = dist
        
        # Find connected location groups (within 200 meters)
        groups = find_connected_groups(distance_matrix, threshold=200)
        
        # Find largest connected group
        largest_group_size = max(len(group) for group in groups)
        
        # Collect detailed information
        distances_info = []
        for i in range(n):
            for j in range(i+1, n):
                dist = distance_matrix[i][j]
                distances_info.append({
                    'location1': f"Location{i+1}({locations[i][0]:.6f},{locations[i][1]:.6f})",
                    'location2': f"Location{j+1}({locations[j][0]:.6f},{locations[j][1]:.6f})",
                    'distance_meters': round(dist, 2),
                    'within_200m': dist <= 200
                })
        
        # Count number of location pairs within 200 meters
        close_pairs = sum(1 for info in distances_info if info['within_200m'])
        
        result = {
            'test_round': test_round,
            'cluster_label': largest_group_size,
            'largest_group_size': largest_group_size,
            'total_groups': len(groups),
            'close_pairs_count': close_pairs,
            'all_distances': distances_info,
            'group_details': groups
        }
        
        results.append(result)
        
        # Print progress
        if test_round % 20 == 0:
            print(f"✓ Processed {test_round} test rounds")
    
    return results

def generate_summary_report(results):
    """
    Generate summary report
    """
    print("\n" + "="*80)
    print("Location Distance Analysis Report (Using GeoPy Precise Calculation)")
    print("="*80)
    
    # Create summary DataFrame
    summary_data = []
    for result in results:
        summary_data.append({
            'Test_Round': result['test_round'],
            'Cluster_Label': result['cluster_label'],
            'Largest_Group_Size': result['largest_group_size'],
            'Total_Groups': result['total_groups'],
            'Close_Pairs_Count': result['close_pairs_count']
        })
    
    summary_df = pd.DataFrame(summary_data)
    
    # Save summary results
    summary_df.to_csv("location_cluster_analysis_summary_geopy.csv", index=False)
    print("✓ Summary results saved to location_cluster_analysis_summary_geopy.csv")
    
    # Calculate label distribution statistics
    label_counts = summary_df['Cluster_Label'].value_counts().sort_index()
    print(f"\nLabel Distribution Statistics:")
    print(f"{'Label':<10} {'Meaning':<40} {'Count':<10} {'Percentage':<10}")
    print("-" * 70)
    
    total_rounds = len(summary_df)
    for label, count in label_counts.items():
        if label == 1:
            meaning = "All 5 locations >200m apart"
        elif label == 2:
            meaning = "Max 2 locations within 200m"
        elif label == 3:
            meaning = "Max 3 locations within 200m"
        elif label == 4:
            meaning = "Max 4 locations within 200m"
        elif label == 5:
            meaning = "All 5 locations within 200m"
        else:
            meaning = f"Max {label} locations within 200m"
        
        percentage = (count / total_rounds) * 100
        print(f"{label:<10} {meaning:<40} {count:<10} {percentage:.1f}%")
    
    return summary_df

def main_geopy():
    """
    Main function using GeoPy
    """
    print("Starting location distance analysis using GeoPy...")
    print("GeoPy advantages:")
    print("- Considers Earth's ellipsoidal shape for more accuracy")
    print("- Handles calculations near poles and international date line")
    print("- Supports multiple distance calculation methods")
    print()
    
    try:
        # Analyze data
        results = analyze_location_clusters_geopy()
        
        # Generate summary report
        summary_df = generate_summary_report(results)
        
        print(f"\n" + "="*80)
        print("Analysis completed! Using GeoPy precise calculation")
        print("="*80)
        
        return results, summary_df
        
    except Exception as e:
        print(f"✗ Error occurred during analysis: {e}")
        return None, None

if __name__ == "__main__":
    results, summary_df = main_geopy()