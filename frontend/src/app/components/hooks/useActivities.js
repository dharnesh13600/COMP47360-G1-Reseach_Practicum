import {useEffect,useState} from 'react';
import { fetchActivities } from '../utils/apiHelpers';


export default function useActivities(){
    const [activities, setActivities] = useState([]);
    const [loading,setLoading]=useState(true);
    const [error,setError]=useState(true);

    useEffect(() => {
  async function getActivities() {
    try{
         const data = await fetchActivities();
        setActivities(data);
    }
    catch(err){
        console.error("Error fetching activities:",err);
        setError(false);
    }
    finally{
        setLoading(false);
    }
   
  }
  getActivities();
}, []);

return {activities,loading,error};
}