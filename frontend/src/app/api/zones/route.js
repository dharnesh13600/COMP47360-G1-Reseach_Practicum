export async function fetchZones(){
    const res=await fetch("http://34.94.236.85/api/recommendations/zones");
    const json=await res.json();
      const zones = json;
    
    return zones;

}