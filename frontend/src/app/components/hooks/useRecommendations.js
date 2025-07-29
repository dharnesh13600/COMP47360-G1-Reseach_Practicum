import {parse,format} from 'date-fns';
const BACKEND_BASE = process.env.NEXT_PUBLIC_BACKEND_API_URL;


export default function useRecommendations(
    
    onSubmit,
    onZoneResults
   
){

async function handleSubmit({ selectedDate, selectedTime, activityChoice ,setSubmitted, setHasSubmittedOnce}){
  if(!activityChoice?.name || !selectedDate || !selectedTime){
    alert("Please select activity, date and time");
    return;
  }
 

  try{
    const date = parse(`${selectedDate} ${selectedTime}`, 'MMMM d HH:mm', new Date());
    const readableTimeJson = format(date, "yyyy-MM-dd'T'HH:mm");

    const payload={
      activity:activityChoice.name,
      dateTime:readableTimeJson,
    };

    const res = await fetch(`${BACKEND_BASE}/api/recommendations` ,{
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ activity: activityChoice.name, dateTime: readableTimeJson }),
    });
    
    if (!res.ok) {
      console.error('Fetch failed', await res.text());
      return;
    }
    
    const { locations } = await res.json();
     onSubmit(locations);
     
      setSubmitted(true);
      setHasSubmittedOnce(true);

    console.log("📬 Response Status:", res.status, res.statusText);
    console.log("📬 Response OK:", res.ok);
    console.log("📬 Response Headers:", [...res.headers.entries()]);

   
  }
  catch(error){
    console.error("Error in handleSubmit:", error);
  }
};

async function handleZoneClick({ selectedDate, selectedTime, activityChoice, area, setZone }){
  if (!selectedDate || !selectedTime) {
    alert('Please select a date AND time before choosing a zone.');
    return;
  }
 

  try{
    const date = parse(`${selectedDate} ${selectedTime}`, 'MMMM d HH:mm', new Date());
    const readableTimeJson =format(date, "yyyy-MM-dd'T'HH:mm");
    console.log(activityChoice);

    const payload={
      activity:activityChoice.name,
      dateTime:readableTimeJson,
      selectedZone:area,
    }

    console.log("Submitting with zone: ",payload);

    const res=await fetch(`${BACKEND_BASE}/api/recommendations`,{
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body:JSON.stringify(payload),
    });

    if(!res.ok){
      console.error("HTTP Error:", res.status, res.statusText);
      return;
    }

    const data=await res.json();
    if (!data) {
      console.error("Empty response received");
      return;
    }
    console.log("Zone success: ", data);
    onZoneResults?.(data, area);
    setZone(data);
  }
  catch (error) {
    console.error("Error in handleZoneClick:", error);
  }
   
};

return {handleSubmit,handleZoneClick};
}



