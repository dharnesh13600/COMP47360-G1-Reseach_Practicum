// utils/mapMarkerHelpers.js
import mapboxgl from 'mapbox-gl';

export function getMarkerElement(index) {
  const el = document.createElement('div');
  el.className = 'numbered-marker';
  el.innerHTML = `
    <div class="pinShape">
      <div class="number">${index + 1}</div>
    </div>
  `;
  const pinShapeEl = el.querySelector('.pinShape');
  pinShapeEl.classList.add('pulse-marker');
  return el;
}

export function getZoneMarkerElement(zone) {
  const el = document.createElement('img');
  const iconMap = {
    Quiet: 'quiet-pin.png',
    Moderate: 'moderate-pin.png',
    Busy: 'busy_pin.png',
    default: 'quiet-pin.png'
  };
  el.src = iconMap[zone.crowdLevel] || iconMap.default;
  el.style.width = '30px';
  el.style.height = '40px';
  el.style.cursor = 'pointer';
  return el;
}

export function getPopupHTML(data, index) {
  return `
  <div class="popup-card">
    <div class="popup-header">
      <span class="info-icon" title="Muse Score: visitor rating. Crowd Estimate: number of visitors. Status: how busy it feels.">i</span>
    </div>
    <div class="muse-score ">Muse Score</div>
    <div class="muse-value">${data.museScore}/10</div>
    <div class="estimate-crowd-label ">Estimate Crowd</div>
    <div class="estimate-crowd">${data.estimatedCrowdNumber}</div>
    <div class="crowd-label ">Crowd Status</div>
    <div class="crowd-status">${data.crowdLevel}</div>
    <div class="directions">
      <img class="directions-image" src="/directions-icon.png" alt="Directions" />
      <button class="directions-button" id="gmaps-${index}">View on Google Maps</button>
    </div>
    <button class="compare-button" id="compare-${index}">
      <svg class="compare-icon" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
        <path d="M8 1v14M1 8h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      Add to Compare
    </button>
    <div class="tooltip">
      <p><b>MUSE SCORE</b> is the product of our machine learning model to calculate 
      the most suitable location for your activity according to busyness 
      and past events in each location. <br><br>
      Don't want to use our Muse Score? Use our predicted <b> Estimate Crowd</b> and <b> Crowd Status </b>
      to pick the best time to be your best self.</p>
    </div>
  </div>
  `;
}
