import {useEffect,useState} from 'react';
import {format} from 'date-fns';
import { fetchDateTimes } from '../utils/apiHelpers';


export default function useDateTimes(selectedDate=null){
    const [rawDateTimes,setRawDateTimes]=useState([]);
    const [availableDates,setAvailableDates]=useState([]);
    const [availableTimes,setAvailableTimes]=useState([]);
    const [loading,setLoading]=useState(true);
    const [error,setError]=useState(true);


    useEffect(()=>{
        async function loadData(){
            setLoading(true);
            try{
                const data=await fetchDateTimes();
                setRawDateTimes(data);

                const dateSet=new Set();
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    data.forEach(entry =>{
      const dateObj=new Date(entry);
     dateObj.setHours(0, 0, 0, 0);
if (dateObj >= today) {
      const formattedDate= format(dateObj,'MMMM d');
      dateSet.add(formattedDate);
}
    });
    setAvailableDates(Array.from(dateSet));
            }
            catch(err){
                 console.error("Error fetching activities:",err);
        setError(false);
            }
            finally{
                   setLoading(false);
            }
        }
        loadData();
    },[]);


    useEffect(() => {
    if (!selectedDate || rawDateTimes.length === 0) {
      setAvailableTimes([]);
      return;
    }

    const matchingTimes = new Set();
    rawDateTimes.forEach((entry) => {
      const dateObj = new Date(entry);
      const dateStr = format(dateObj, 'MMMM d');
      const timeStr = format(dateObj, 'HH:mm');

      if (dateStr === selectedDate) {
        matchingTimes.add(timeStr);
      }
    });

    const sortedTimes = Array.from(matchingTimes).sort((a, b) => {
      const [aH, aM] = a.split(':').map(Number);
      const [bH, bM] = b.split(':').map(Number);
      const aShift = aH < 6 ? aH + 24 : aH;
      const bShift = bH < 6 ? bH + 24 : bH;
      return aShift * 60 + aM - (bShift * 60 + bM);
    });

    setAvailableTimes(sortedTimes);
  }, [selectedDate, rawDateTimes]);
    return {
    availableDates,
    availableTimes,
    loading,
    error,
  };

}