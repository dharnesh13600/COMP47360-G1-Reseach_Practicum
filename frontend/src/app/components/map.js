// https://docs.mapbox.com/help/tutorials/use-mapbox-gl-js-with-react/?step=3

'use client';

import mapboxgl from 'mapbox-gl';
import {useRef,useEffect,useState} from 'react';
import 'mapbox-gl/dist/mapbox-gl.css';
import '../styles/map.css';
import ComparisonStack from './comparisonStack.js';

import '../globals.css';

import { maxTime } from 'date-fns/constants';

import Image from 'next/image';




const useMapbox = process.env.NEXT_PUBLIC_USE_MAPBOX==='true';

if (useMapbox) {
  mapboxgl.accessToken = process.env.NEXT_PUBLIC_MAPBOX_TOKEN;
}

const INITIAL_CENTER =[

 -74.000, 40.7526
]


const INITIAL_ZOOM=11.57




const viewConfigs = [
  { minWidth: 320, zoom: 10.30, center: INITIAL_CENTER ,pitch:0,bearing:30},
  { minWidth: 768, zoom: 11, center: INITIAL_CENTER, pitch: 50, bearing: 30 },
  { minWidth: 900, zoom: 11.75, center: INITIAL_CENTER, pitch: 10, bearing: -10 },
];
function getViewConfig() {
  const width = window.innerWidth;

  // find all configs where minWidth <= width
  const applicableConfigs = viewConfigs.filter(cfg => width >= cfg.minWidth);

  if (applicableConfigs.length > 0) {
    // pick the one with largest minWidth
    return applicableConfigs.reduce((prev, curr) =>
      curr.minWidth > prev.minWidth ? curr : prev
    );
  }

  // fallback
  return viewConfigs[0];
}

export default function Map({ submitted, locations, selectedLocation }){
const popupRef = useRef();
const [showMarkers, setShowMarkers] = useState(false);

    const mapRef =useRef()
    const mapContainerRef= useRef()

    const [center, setCenter]=useState(INITIAL_CENTER)
    const [zoom,setZoom]=useState(INITIAL_ZOOM)

   const markersRef = useRef([]);

  const activePopupRef = useRef(null);
  const [comparisonStack, setComparisonStack] = useState([]);


const lastClickedMarkerRef = useRef(null);


const removeItem = (id) => {
  setComparisonStack(prev => prev.filter(item => item.id !== id));
};

 
async function fetchPlaceId(lat, lng) {
  const apiKey = process.env.NEXT_PUBLIC_GOOGLE_TOKEN;  
  console.log(process.env.NEXT_PUBLIC_GOOGLE_TOKEN);
  const url = `https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${apiKey}`;

  const res = await fetch(url);
  const data = await res.json();
console.log('Google API response:', data);
  if (data.status === "OK" && data.results.length > 0) {
    const placeId = data.results[0].place_id;
    return placeId;
  } else {
    throw new Error("No place found");
  }
}


const openInGoogleMaps = async (lat, lng) => {
  try {
    const placeId = await fetchPlaceId(lat, lng);
    window.open(`https://www.google.com/maps/place/?q=place_id:${placeId}`, '_blank');
  } catch (err) {
    console.error(err);
    alert("Could not find place in Google Maps.");
  }
};


    useEffect(() => {
    if (!useMapbox) return;

    if (mapRef.current) return;

     
    
    mapRef.current = new mapboxgl.Map({
      container: mapContainerRef.current,
      style:process.env.NEXT_PUBLIC_MAPBOX_STYLE_URL,
      center: center,
      zoom: zoom,
      pitch:50,
      bearing:-20
    });

mapRef.current.addControl(new mapboxgl.NavigationControl());
const adjustView = () => {
  const config = getViewConfig();
   if (!mapRef.current) return;
  mapRef.current.flyTo({
    center: config.center,
    zoom: config.zoom,
    duration: 1000,
    pitch: config.pitch ?? mapRef.current.getPitch(),
    bearing: config.bearing ?? mapRef.current.getBearing()
  });
};
    mapRef.current.on('load', () => {
  mapRef.current.resize();
  adjustView();
  window.addEventListener('resize',adjustView);
});

    mapRef.current.on('move', () => {
      const mapCenter = mapRef.current.getCenter();
      const mapZoom = mapRef.current.getZoom();

       const newCenter = [mapCenter.lng, mapCenter.lat];
  const newZoom = mapZoom;

        if (
    newCenter[0] !== center[0] ||
    newCenter[1] !== center[1] ||
    newZoom !== zoom
  ) {
    setCenter(newCenter);
    setZoom(newZoom);
  }
      markersRef.current.forEach(marker => {
        const el = marker.getElement();
      if (mapZoom < 14) {
    el.style.opacity = '0.5';
    el.title = 'Zoom in to interact';
  } else {
    el.style.opacity = '1';
    el.title = '';
  }
      });

   
});






      
    
     return () => {
      if (mapRef.current) {
        mapRef.current.remove();
        mapRef.current = null; 
      }
       markersRef.current.forEach(marker => marker.remove());
      markersRef.current = [];

    };



  }, [])

  useEffect(() => {
    if (!selectedLocation || !mapRef.current) return;

    const map = mapRef.current;

    if (selectedLocation.longitude == null || selectedLocation.latitude == null) return;

  
 
    map.flyTo({
      center: [selectedLocation.longitude, selectedLocation.latitude],
      zoom: 18,
      pitch: 60,
      bearing: 0,
      duration: 3000,
      easing: t => t,
    });

   if (activePopupRef.current) {
      activePopupRef.current.remove();
      activePopupRef.current = null;
    } 
   
  

  
    const marker = markersRef.current.find(m => {
      const lngLat = m.getLngLat();
      return (
        Math.abs(lngLat.lng - selectedLocation.longitude) < 0.0001 &&
        Math.abs(lngLat.lat - selectedLocation.latitude) < 0.0001
      );
    });

    if (marker) {
      marker.getPopup().addTo(map);
      activePopupRef.current = marker.getPopup();
    }

  }, [selectedLocation]);


  useEffect(() => {
  // if (!mapRef.current || !locations.length) return;
  if (!mapRef.current || !(locations?.length > 0)) return;

  // Remove existing markers
  markersRef.current.forEach(m => m.remove());
  markersRef.current = [];

   locations.forEach((location, index) => {
      const el = document.createElement('div');
      el.className = 'numbered-marker';
      el.innerHTML = `<div class="pinShape"><div class="number">${index+1}</div></div>`;

      const popup = new mapboxgl.Popup({ offset: 25 }).setHTML(`
    <div class="popup-card">
    <div class="popup-header">
    <span class="info-icon" title="Muse Score: visitor rating. Crowd Estimate: number of visitors. Status: how busy it feels.">i</span>
  </div>
    <div class="muse-score ">Muse Score</div>
    <div class="muse-value">8/10</div>
    <div class="estimate-crowd-label ">Estimate Crowd  </div>
    <div class="estimate-crowd">4537</div>
    <div class="crowd-label ">Crowd Status </div>
    <div class="crowd-status">Busy</div>
    <div class="directions">
    <img class="directions-image" src="/directions-icon.png" alt="Directions" />
    <button class="directions-button" id="gmaps-${index}">View on Google Maps</button>
    </div>

    <!-- ComparisonStack placeholder directly under the Directions button -->
    <button class="compare-button" id="compare-${index}">
<svg class="compare-icon" width="16" height="16" fill="currentColor" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
    <path d="M8 1v14M1 8h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
  </svg>
    Add to Compare
    </button>

    <div class="tooltip">
    <p><b>MUSE SCORE</b> is the product of our machine learning model to calculate 
    the most suitable location for your activity according to busyness 
    and past events in each location. <br><br>

    Don't want to use our Muse Score? Use our predicted <b> Estimate Crowd</b> and <b> Crowd Status </b> the best time to be your best self,
    whether in the crowd or in a peaceful corner.

    </p>
    </div>
  </div>
`);
      
const addToComparison = (location) => {
  setComparisonStack(prev => {
    if (prev.find(item => item.id === location.id)) return prev; // prevent duplicates
    if (prev.length >= 3) return prev; // max 5
    return [...prev, location];
  });
};

 const marker=new mapboxgl.Marker(el)
        .setLngLat([location.longitude, location.latitude])
        .setPopup(popup)
        .addTo(mapRef.current);
          markersRef.current.push(marker);
          
 el.addEventListener('click', () => {
        const currentBearing = mapRef.current.getBearing();
      const currentZoom = mapRef.current.getZoom();

    
        
      if (
  lastClickedMarkerRef.current &&
  lastClickedMarkerRef.current === marker
) {if (activePopupRef.current) {
      activePopupRef.current.remove();
      activePopupRef.current = null;
    } 
    mapRef.current.flyTo({
    center: INITIAL_CENTER,
    zoom: INITIAL_ZOOM,
    pitch: 0,
    bearing: 0,
    duration: 3000
  });
  lastClickedMarkerRef.current = null;
  return;
  }
 if (activePopupRef.current) {
    activePopupRef.current.remove();
    activePopupRef.current = null;
  }
        mapRef.current.easeTo({
          center: [location.longitude, location.latitude],
          zoom: 18,          
          pitch: 60,        
          bearing: currentBearing + 360,
          duration: 3000,   
          easing: t => t    
        });
marker.getPopup().addTo(mapRef.current);
  activePopupRef.current = marker.getPopup();
  lastClickedMarkerRef.current = marker;
});
popup.on('open', () => {
    const popupEl = popup.getElement();
    const icon = popupEl.querySelector('.info-icon');
    const tooltip = popupEl.querySelector('.tooltip');

    if (!icon || !tooltip) return;

    icon.addEventListener('click', (e) => {
      e.stopPropagation();
      const isVisible = tooltip.style.display === 'block';
      tooltip.style.display = isVisible ? 'none' : 'block';
    });
   
const btn = document.getElementById(`gmaps-${index}`);
  if (btn) {
    btn.addEventListener('click', () => {
      openInGoogleMaps(location.latitude, location.longitude);
    });
  }
//// COMPARE BUTTON  
const compareBtn = document.getElementById(`compare-${index}`);

if (compareBtn) {
  compareBtn.addEventListener('click', () => {
    addToComparison({
      id: location.id || index,
      locName: location.name || `location name`,
      selectedLat: location.latitude,
      selectedLong: location.longitude,
      museScore: location.museScore,
      estimateCrowd: location.estimateCrowd ,
      crowdStatus: location.crowdStatus 
    });
  });
}
      


      });
 
      const pinShapeEl = el.querySelector('.pinShape');
  pinShapeEl.classList.add('pulse-marker');

 

  

    
  });
}, [locations]);


  if (!useMapbox) {
    return (
      <div id="map-container">
        <p style={{ textAlign: 'center', paddingTop: '2rem' }}>
            Mapbox is disabled in development to avoid usage.
        </p>
       
      </div>
    );
  }

  const handleButtonClick = () => {
     if (activePopupRef.current) {
    activePopupRef.current.remove();
    activePopupRef.current = null;
  }
  mapRef.current.flyTo({
    center: INITIAL_CENTER,
    zoom: INITIAL_ZOOM,
    pitch: 0,
    bearing: 0,
    duration: 3000
  })
}
      
    return (
        <>
        
            <button className='reset-button' onClick={handleButtonClick}>
                Reset View
            </button>
            <div id='map-container' ref={mapContainerRef}>
             
            </div>
            <ComparisonStack
              stack={comparisonStack}
              clearStack={() => setComparisonStack([])}
              removeItem={removeItem}
            />

        </>
    )
}


