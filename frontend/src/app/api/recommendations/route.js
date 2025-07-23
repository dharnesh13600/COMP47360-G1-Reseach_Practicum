import { NextResponse } from 'next/server';

export async function POST(req) {
  try {
    const jsonBody = await req.json(); 
    const { activity, dateTime, selectedZone } = jsonBody;

    if (!activity || !dateTime) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 });
    }

    return NextResponse.json({
      activity,
      dateTime,
      selectedZone
    });
  } catch (err) {
    console.error("API error:", err);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
