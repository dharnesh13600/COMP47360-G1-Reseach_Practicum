import { NextResponse } from 'next/server';

export async function POST(req) {
  try {
    const { activity, dateTime, selectedZone } = await req.json();

    const body = selectedZone
      ? { activity, dateTime, selectedZone }
      : { activity, dateTime };

    console.log('🔗 [fetchLocations] sending to backend:', body);

    const backendUrl = process.env.BACKEND_API_URL;
    console.log(backendUrl)

    if (!backendUrl) {
      throw new Error('BACKEND_API_URL is not defined in environment variables');
    }

    const upstream = await fetch(`${backendUrl}/recommendations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });

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
