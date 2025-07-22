
export async function fetchActivities(){
    const res=await fetch("GET /api/recommendations/activities");
    const json=await res.json();
    const activities=json.map(item=>item.activity);
    return activities;

}