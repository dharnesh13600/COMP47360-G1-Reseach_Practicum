
export async function getWeatherData() {
  const response = await fetch('forecast.json'); // or full path if needed
  if (!response.ok) {
    throw new Error('Failed to fetch weather data');
  }

  const data = await response.json();
  return data;
}