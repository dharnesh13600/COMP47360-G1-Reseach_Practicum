// https://nextjs.org/docs/app/getting-started/fetching-data
export default async function GetLocationData() {
  const response = await fetch('location.json',{cache: 'no-store'}); 

  if (!response.ok) {
    throw new Error('Failed to fetch location data');
  }

  const posts=await response.json();
  return posts;
}