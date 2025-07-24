// export async function fetchZones(){
//     const res=await fetch("http://34.94.236.85/api/recommendations/zones");
//     const json=await res.json();
//       const zones = json;
    
//     return zones;

// }


import { NextResponse } from 'next/server';

export async function GET() {
  try {
    const res = await fetch(`${process.env.BACKEND_API_URL}/recommendations/zones`, {
      headers: {
        'Content-Type': 'application/json',
        
      },
    });

    if (!res.ok) {
      throw new Error(`Failed to fetch zones: ${res.statusText}`);
    }

    const data = await res.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Error in /api/zones:', error);
    return NextResponse.json({ error: 'Failed to load zones' }, { status: 500 });
  }
}