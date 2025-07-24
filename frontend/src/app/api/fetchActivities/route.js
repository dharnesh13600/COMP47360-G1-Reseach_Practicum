
// export async function fetchActivities(){
//     const res=await fetch("http://34.94.236.85/api/recommendations/activities");
//     const json=await res.json();
//     const activities=json.map(item=>item.name);
//     console.log(activities);
//     return activities;

// }


import { NextResponse } from 'next/server';

export async function GET() {
  try {
    const res = await fetch(`${process.env.BACKEND_API_URL}/recommendations/activities`, {
      headers: {
        method: 'GET',
        'Content-Type': 'application/json',
      },
    });

    if (!res.ok) {
      throw new Error(`Failed to fetch activities: ${res.statusText}`);
    }

    const backendData = await res.json();

    // Optionally transform it before returning
    const activities = backendData.map(item => ({
      id: item.id,
      name: item.name,
    }));

    return NextResponse.json(activities);
  } catch (error) {
    console.error('Error in fetchActivities route:', error);
    return NextResponse.json({ error: 'Failed to load activities' }, { status: 500 });
  }
}