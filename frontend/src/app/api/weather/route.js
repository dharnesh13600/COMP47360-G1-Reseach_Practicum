// https://nextjs.org/docs/app/getting-started/route-handlers-and-middleware

import {parse,format} from "date-fns";

export async function fetchWeather(selectedDate,selectedTime){
        if(!selectedDate || !selectedTime){
                console.error("Date and time required to fetch weather");
                return null;
        }

        const dateObj= parse(
                `${selectedDate} ${selectedTime}`,
                "MMMM d h:mm",
                new Date()
        );

        const isoDateTime=format(dateObj,"yyyy-MM-dd'T'HH:mm:ss");

        const url=`http://34.94.195.103/api/forecast?datetime=${isoDateTime}`;
        const res=await fetch(url);
        const data=await res.json();

        if(data.error){
                console.error("Weather API error: ",data.message||data.error);
                return null;
        }

        return {
                temperature: data.temperature,
                condition: data.condition
        };
}