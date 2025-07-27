import { useEffect,useState } from "react";
import { fetchWeather } from "../utils/apiHelpers";

export default function useWeatherData(date,time,submitted=true){
    const [weather,setWeather]=useState(null);
    const [loading,setLoading]=useState(true);
    const [error,setError]=useState(true);


    useEffect(()=>{
  if (!date || !time || !submitted){
    setWeather(null);
    return;
  }
  async function getWeather(){
    setLoading(true);
    try{
        const data=await fetchWeather(date,time);
        setWeather(data);
    }
    catch(err){
        console.error("Error fetching weather: ",err);
        setError(data);
    }
    finally{
        setLoading(false);
    }
  }
  getWeather();
},[date,time,submitted]);

return {weather,loading,error};
}