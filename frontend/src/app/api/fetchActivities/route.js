
export async function fetchActivities(){
    const res=await fetch("http://34.94.236.85/api/recommendations/activities");
    const json=await res.json();
    const activities=json.map(item=>item.name);
    console.log(activities);
    return activities;

}