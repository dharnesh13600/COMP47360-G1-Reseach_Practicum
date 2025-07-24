// export async function fetchDateTimes(){
//     const res=await fetch("http://34.94.236.85/api/forecast/available-datetimes");
//     const json=await res.json();
//       const dateTimes = json;
    
//     return dateTimes;

// }


import { NextResponse } from 'next/server';

export async function GET() {
  try {
    const res = await fetch(`${process.env.BACKEND_API_URL}/forecast/available-datetimes`, {
      headers: {
        'Content-Type': 'application/json',
        // Add auth headers if needed
      },
    });

    if (!res.ok) {
      throw new Error(`Failed to fetch datetimes: ${res.statusText}`);
    }

    const data = await res.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Error in /api/fetchDateTimes:', error);
    return NextResponse.json({ error: 'Failed to load datetimes' }, { status: 500 });
  }
}