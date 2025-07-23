// // src/app/api/fetchLocations/route.js
// import { NextResponse } from 'next/server';

// export async function POST(req) {
//   try {
//     // 1) Parse the incoming JSON
//     const { activity, dateTime, selectedZone } = await req.json();

//     // 2) Basic validation
//     if (!activity || !dateTime) {
//       return NextResponse.json(
//         { error: 'Missing required fields' },
//         { status: 400 }
//       );
//     }

//     // 3) Proxy to your upstream service
//     const upstream = await fetch(
//       'http://34.94.236.85/api/recommendations',
//       {
//         method: 'POST',
//         headers: { 'Content-Type': 'application/json' },
//         body: JSON.stringify({ activity, dateTime, selectedZone }),
//       }
//     );
//     if (!upstream.ok) {
//       // You can log upstream.text() here if you want more detail
//       return NextResponse.json(
//         { error: 'Upstream fetch failed' },
//         { status: upstream.status }
//       );
//     }

//     // 4) Extract and return only the locations
//     const data = await upstream.json();
//     return NextResponse.json({ locations: data.locations });
//   } catch (err) {
//     console.error('fetchLocations POST error:', err);
//     return NextResponse.json(
//       { error: 'Internal server error' },
//       { status: 500 }
//     );
//   }
// }


//----------------------------------------------------

// src/app/api/fetchLocations/route.js
// src/app/api/fetchLocations/route.js
import { NextResponse } from 'next/server';

export async function POST(req) {
  try {
    const { activity, dateTime, selectedZone } = await req.json();
    const body = selectedZone != null
      ? { activity, dateTime, selectedZone }
      : { activity, dateTime };

    console.log('🔗 [fetchLocations] sending to backend:', body);

    const upstream = await fetch(
      'http://34.94.236.85/api/recommendations',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      }
    );

    if (!upstream.ok) {
      const text = await upstream.text();
      console.error(
        `❌ [fetchLocations] backend responded ${upstream.status}:`,
        text
      );
      return NextResponse.json(
        { error: 'Upstream fetch failed' },
        { status: upstream.status }
      );
    }

    const data = await upstream.json();
    return NextResponse.json({ locations: data.locations });
  } catch (err) {
    console.error('🔥 [fetchLocations] unexpected error:', err);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
