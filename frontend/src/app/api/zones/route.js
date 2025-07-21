export async function fetchZones(){
    const res=await fetch("GET /api/recommendations/zones");
    const json=await res.json();
    const activities=json.map(item=>item.activity);
    return activities;

}