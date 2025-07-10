'use client';
import {format} from 'date-fns';
import { GetWeatherData } from '../weather-data';


export async function getDateAndTime(){
    const dates=new Set();
    const times=new Set();
    try{
        const data=await getWeatherData();
        data.list.forEach(entry=>{
            const dateObj=new Date(entry.readableTime);

            const formattedDate= format(dateObj,'MMMM d');
            const formattedTime=format(dateObj,'h:mm a');
            dates.add(formattedDate);
            times.add(formattedTime);


        });
        const datesArr=Array.from(dates);
        const timeArr=Array.from(times);
        return {dates:datesArr, times:timeArr};
    }
    catch(err){
        console.error(err);
        return {dates:[], times:[]};
    }
    
}