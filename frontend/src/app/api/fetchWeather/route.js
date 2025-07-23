// https://nextjs.org/docs/app/getting-started/route-handlers-and-middleware

// import {parse,format} from "date-fns";

// export async function fetchWeather(selectedDate,selectedTime){
//         if(!selectedDate || !selectedTime){
//                 console.error("Date and time required to fetch weather");
//                 return null;
//         }

//         const dateObj= parse(
//                 `${selectedDate} ${selectedTime}`,
//                 "MMMM d HH:mm",
//                 new Date()
//         );

//         const isoDateTime=format(dateObj,"yyyy-MM-dd'T'HH:mm:ss");

//         const url=`http://34.94.236.85/api/forecast?datetime=${isoDateTime}`;
//         const res=await fetch(url);
//         const data=await res.json();

//         if(data.error){
//                 console.error("Weather API error: ",data.message||data.error);
//                 return null;
//         }

//         return {
//                 temperature: data.temperature,
//                 condition: data.condition
//         };
// }

import { NextResponse } from 'next/server';
import { parse, format } from 'date-fns';

export async function POST(req) {
  try {
    const body = await req.json();
    const { selectedDate, selectedTime } = body;

    if (!selectedDate || !selectedTime) {
      return NextResponse.json({ error: 'Date and time required' }, { status: 400 });
    }

    // Format to ISO string
    const dateObj = parse(`${selectedDate} ${selectedTime}`, 'MMMM d HH:mm', new Date());
    const isoDateTime = format(dateObj, "yyyy-MM-dd'T'HH:mm:ss");


    const backendUrl = process.env.BACKEND_API_URL;
    const url = `${backendUrl}/forecast?datetime=${isoDateTime}`;

    const res = await fetch(url);
    const data = await res.json();

    if (data.error) {
      return NextResponse.json({ error: data.message || data.error }, { status: 500 });
    }

    return NextResponse.json({
      temperature: data.temperature,
      condition: data.condition,
    });
  } catch (err) {
    console.error('Error in /api/fetchWeather:', err);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}