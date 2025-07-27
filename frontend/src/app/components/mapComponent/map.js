// https://docs.mapbox.com/help/tutorials/use-mapbox-gl-js-with-react/?step=3

'use client';


import {useRef,useEffect,useState,useCallback} from 'react';
import 'mapbox-gl/dist/mapbox-gl.css';
import './map.css';
import ComparisonStack from './comparisonStack.js';


import { maxTime } from 'date-fns/constants';
import { parse, format } from 'date-fns';


import Image from 'next/image';

import mapboxgl from 'mapbox-gl';
import { getMarkerElement,getZoneMarkerElement,getPopupHTML } from '../utils/mapMarkerHelpers';
const INITIAL_CENTER =[

 -74.000, 40.7526
]


const INITIAL_ZOOM=11.57

 const useMapbox = process.env.NEXT_PUBLIC_USE_MAPBOX==='true';
 
if (useMapbox) {
  mapboxgl.accessToken = process.env.NEXT_PUBLIC_MAPBOX_TOKEN;
}
const iconMap = {
  Quiet: 'quiet-pin.png',
  Moderate: 'moderate-pin.png',
  Busy: 'busy_pin.png',
  default: 'quiet-pin.png'
};

export default function Map({ submitted, locations, selectedLocation,selectedTime,selectedZone ,zoneLocations,showLocations,clearMarkers,showAllLocations}){
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

const navigationControlRef = useRef(null);

const removeItem = (id) => {
  setComparisonStack(prev => prev.filter(item => item.id !== id));
};

 
async function fetchPlaceId(lat, lng) {
  const apiKey = process.env.NEXT_PUBLIC_GOOGLE_TOKEN;  
  console.log(process.env.NEXT_PUBLIC_GOOGLE_TOKEN);
  console.log(lat);
  console.log(lng);
  const url = `https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${apiKey}`;

  const res = await fetch(url);
  const data = await res.json();
// console.log('Google API response:', data);
  if (data.status === "OK" && data.results.length > 0) {
    const placeId = data.results[0].place_id;
    return placeId;
  } else {
    throw new Error("No place found");
  }
}


const openInGoogleMaps = useCallback(async (lat, lng) => {
  try {
    const placeId = await fetchPlaceId(lat, lng);
    window.open(`https://www.google.com/maps/place/?q=place_id:${placeId}`, '_blank');
  } catch (err) {
    console.error(err);
    alert("Could not find place in Google Maps.");
  }
}, []);

function getStyleFromSelectedTime(selectedTime) {
  const DAY_STYLE = process.env.NEXT_PUBLIC_MAPBOX_STYLE_URL;
  const NIGHT_STYLE = process.env.NEXT_PUBLIC_MAPBOX_STYLE_DARK_URL;

  if (!selectedTime) return DAY_STYLE;

  const parsed = parse(selectedTime.trim(), 'HH:mm', new Date());
  const hour = parsed.getHours();

  return hour >= 18 && hour<=6 ? NIGHT_STYLE : DAY_STYLE;
}
// initializing mapbox

useEffect(() => {
       const mapboxgl = require('mapbox-gl');
     
    if (!useMapbox) return;

    if (mapRef.current) return;

    







const viewConfigs = [
  { minWidth: 320, zoom: 10.30, center: INITIAL_CENTER ,pitch:10,bearing:20},
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
    
    mapRef.current = new mapboxgl.Map({
      container: mapContainerRef.current,
      style:process.env.NEXT_PUBLIC_MAPBOX_STYLE_URL,
      center: center,
      zoom: zoom,
      pitch:50,
      bearing:-20
    });
navigationControlRef.current = new mapboxgl.NavigationControl();
mapRef.current.addControl(navigationControlRef.current, 'top-right');
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
  if (!mapRef.current || !selectedTime) return;

  const styleUrl = getStyleFromSelectedTime(selectedTime);
  mapRef.current.setStyle(styleUrl);
mapRef.current.on('style.load', () => {
    if (!navigationControlRef.current) {
      navigationControlRef.current = new mapboxgl.NavigationControl();
    }

    // Check if control already exists
    const controls = mapRef.current._controls || [];
    const alreadyAdded = controls.includes(navigationControlRef.current);
    if (!alreadyAdded) {
      mapRef.current.addControl(navigationControlRef.current, 'top-right');
    }
  });
}, [selectedTime]);

  useEffect(() => {
    if (!selectedLocation || !mapRef.current) return;

    const map = mapRef.current;

    if (selectedLocation.longitude == null || selectedLocation.latitude == null) return;

  
 
    map.flyTo({
      center: [selectedLocation.longitude, selectedLocation.latitude],
      zoom: 14,
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

  }, [selectedLocation?.latitude, selectedLocation?.longitude]);

  useEffect(() => {
    if (!selectedZone|| !mapRef.current) return;

    const map = mapRef.current;

    if (selectedZone.longitude == null || selectedZone.latitude == null) return;

  
 
    map.flyTo({
      center: [selectedZone.longitude, selectedZone.latitude],
      zoom: 14,
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

  }, [selectedZone]);



  useEffect(() => {
    if (!mapRef.current) return;

    if (!submitted || clearMarkers ) {
  markersRef.current.forEach(m => m.remove());
  markersRef.current = [];
  return;
}

    markersRef.current.forEach(m => m.remove());
    markersRef.current = [];

    const toDraw = showLocations
  ? (showAllLocations ? locations.slice(0, 10)  : locations.slice(0, 5))
  : zoneLocations;

  toDraw.forEach((item, index) => {
    if (showLocations) {
      const loc=item;
      const el = getMarkerElement(index);
      const popup = new mapboxgl.Popup({ offset: 25 }).setHTML(getPopupHTML(loc, index));




const addToComparison = (loc) => {
  setComparisonStack(prev => {
    if (prev.find(item => item.id === loc.id)) return prev; // prevent duplicates
    if (prev.length >= 3) return prev; // max 5
    return [...prev, loc];
  });
};

        const marker = new mapboxgl.Marker(el)
          .setLngLat([loc.longitude, loc.latitude])
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
          center: [loc.longitude, loc.latitude],
          zoom: 16,          
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
      openInGoogleMaps(loc.latitude, loc.longitude);
    });
  }
//// COMPARE BUTTON  
const compareBtn = document.getElementById(`compare-${index}`);

if (compareBtn) {
  compareBtn.addEventListener('click', () => {
    addToComparison({
      id: loc.id || index,
      locName: (loc.zoneName || `location name`).split(' ').slice(0, 3).join(' '),
      selectedLat: loc.latitude,
      selectedLong: loc.longitude,
      museScore: loc.museScore,
      estimateCrowd: loc.estimatedCrowdNumber ,
      crowdStatus: loc.crowdLevel
    });
  });
}
      


      });
 
      const pinShapeEl = el.querySelector('.pinShape');
  pinShapeEl.classList.add('pulse-marker');

    } 
    else {
   
  markersRef.current.forEach(m => m.remove());
  markersRef.current = [];

  const zone=item;
 zoneLocations.forEach((zone, index) => {
const el = getZoneMarkerElement(zone);
const popup = new mapboxgl.Popup({ offset: 25 }).setHTML(getPopupHTML(zone, index));
const addToComparison = (zone) => {
  setComparisonStack(prev => {
    if (prev.find(item => item.id === zone.id)) return prev; // prevent duplicates
    if (prev.length >= 3) return prev; // max 5
    return [...prev, zone];
  });
};
    const marker = new mapboxgl.Marker({ element: el, anchor: 'bottom' })
      .setLngLat([ zone.longitude, zone.latitude ])
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
          center: [zone.longitude, zone.latitude],
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
      openInGoogleMaps(zone.latitude, zone.longitude);
    });
  }
//// COMPARE BUTTON  
const compareBtn = document.getElementById(`compare-${index}`);

if (compareBtn) {
  compareBtn.addEventListener('click', () => {
    addToComparison({
      id: zone.id || index,
      locName: (zone.zoneName || `zone name`).split(' ').slice(0, 3).join(' '),
      selectedLat: zone.latitude,
      selectedLong: zone.longitude,
      museScore: zone.museScore,
      estimateCrowd: zone.estimatedCrowdNumber ,
      crowdStatus: zone.crowdLevel
    });
  });

}
      


      });
    });
  }
  });
  return () => {
    markersRef.current.forEach(m => m.remove());
    markersRef.current = [];
  };
  }, [submitted,clearMarkers,showLocations, locations, zoneLocations, showAllLocations, openInGoogleMaps]);


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


