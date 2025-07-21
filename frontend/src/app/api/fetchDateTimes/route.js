export async function fetchDateTimes(){
    const res=await fetch("http://34.94.195.103/api/forecast/available-datetimes");
    const json=await res.json();
      const dateTimes = json;
    
    return dateTimes;

}