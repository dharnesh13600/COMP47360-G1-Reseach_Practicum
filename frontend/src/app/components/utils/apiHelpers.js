import { parse, format } from 'date-fns';
export const BACKEND_BASE = process.env.NEXT_PUBLIC_BACKEND_API_URL;



export async function fetchActivities() {
  const res = await fetch(`${BACKEND_BASE}/api/recommendations/activities`);
  if (!res.ok) throw new Error('Failed to fetch activities');
  return res.json();
}


export async function fetchDateTimes() {
  const res = await fetch(`${BACKEND_BASE}/api/forecast/available-datetimes`);
  if (!res.ok) throw new Error('Failed to fetch date-times');
  return res.json();
}


 export async function fetchWeather(date, time) {
  const dateObj = parse(`${date} ${time}`, 'MMMM d HH:mm', new Date());
  const isoDateTime = format(dateObj, "yyyy-MM-dd'T'HH:mm:ss");

  const res = await fetch(`${BACKEND_BASE}/api/forecast?datetime=${isoDateTime}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });

  if (!res.ok) throw new Error('Failed to fetch weather');

  return res.json();
}

export async function fetchZones() {
  const res = await fetch(`${BACKEND_BASE}/api/recommendations/zones`);
  if (!res.ok) throw new Error('Failed to fetch zones');
  return res.json();
}