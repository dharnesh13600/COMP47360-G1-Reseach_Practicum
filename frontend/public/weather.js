// async function populate() {
//   const requestURL =
//     "forecast.json";
//   const request = new Request(requestURL);

//   const response = await fetch(request);
//   const superHeroes = await response.json();

//   weatherData(superHeroes);
// }

// export default weatherData;

// weatherData.js

export async function getWeatherData() {
  const response = await fetch('forecast.json'); // or full path if needed
  if (!response.ok) {
    throw new Error('Failed to fetch weather data');
  }

  const data = await response.json();
  return data;
}
