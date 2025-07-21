export async function fetchLocations(){
    const res=await fetch("http://34.94.195.103/api/recommendations");
    const json=await res.json();
    const locations=json.locations;
   
return locations;
}