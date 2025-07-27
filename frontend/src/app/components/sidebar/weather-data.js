
// https://nextjs.org/docs/app/getting-started/fetching-data
export async function GetWeatherData() {
  const response = await fetch('forecast.json'); 
  if (!response.ok) {
    throw new Error('Failed to fetch weather data');
  }

  const data = await response.json();
  return data;
}