// https://docs.mapbox.com/help/tutorials/use-mapbox-gl-js-with-react/?step=3

'use client';

import mapboxgl from 'mapbox-gl';
import {useRef,useEffect,useState} from 'react';
import 'mapbox-gl/dist/mapbox-gl.css';
import '../styles/map.css';

import '../globals.css';



const useMapbox = process.env.NEXT_PUBLIC_USE_MAPBOX==='true';

if (useMapbox) {
  mapboxgl.accessToken = process.env.NEXT_PUBLIC_MAPBOX_TOKEN;
}

const INITIAL_CENTER =[
    -74.006,
    40.7822
]
const INITIAL_ZOOM=11.25

const locationsData = [
  {
    zoneName: "Battery Park: South Tip",
    latitude: 40.703277,   // southern tip
    longitude: -74.017028,
  },
  {
    zoneName: "Wall Street: Financial District",
    latitude: 40.707491,
    longitude: -74.011276,
  },
  {
    zoneName: "Union Square Park",
    latitude: 40.735863,
    longitude: -73.991084,
  },
  {
    zoneName: "Times Square: 7th Ave & 42nd",
    latitude: 40.758896,
    longitude: -73.985130,
  },
  {
    zoneName: "Central Park: Bethesda Terrace",
    latitude: 40.774036,
    longitude: -73.970913,
  },
  {
    zoneName: "Upper West Side: 96th Street",
    latitude: 40.793919,
    longitude: -73.972323,
  },
  {
    zoneName: "Harlem: 125th Street",
    latitude: 40.807536,
    longitude: -73.945713,
  },
  {
    zoneName: "Inwood Hill Park: Northern Tip",
    latitude: 40.872028,
    longitude: -73.923790,
  }
];


export default function Map({ submitted, locations, selectedLocation }){
const popupRef = useRef();
const [showMarkers, setShowMarkers] = useState(false);

    const mapRef =useRef()
    const mapContainerRef= useRef()

    const [center, setCenter]=useState(INITIAL_CENTER)
    const [zoom,setZoom]=useState(INITIAL_ZOOM)

   const markersRef = useRef([]);

  const activePopupRef = useRef(null);



 



    useEffect(() => {
    if (!useMapbox) return;

    if (mapRef.current) return;

     
    
    mapRef.current = new mapboxgl.Map({
      container: mapContainerRef.current,
      style:process.env.NEXT_PUBLIC_MAPBOX_STYLE_URL,
      center: center,
      zoom: zoom,
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


locationsData.forEach((location, index) => {
      const el = document.createElement('div');
      el.className = 'numbered-marker';
      el.innerHTML = `<div class="pinShape"><div class="number">${index+1}</div></div>`;

      const popup = new mapboxgl.Popup({ offset: 25 }).setHTML(`
    <div class="popup-card">
    <div class="muse-score">Muse Score</div>
    <div class="muse-value">${location.museScore ?? '--'}</div>
    <div class="estimate-crowd-label">Estimate Crowd</div>
    <div class="estimate-crowd">--</div>
    <div class="crowd-label">Crowd</div>
    <div class="crowd-status">--</div>
  </div>
`);

      const marker=new mapboxgl.Marker(el)
        .setLngLat([location.longitude, location.latitude])
        .setPopup(popup)
        .addTo(mapRef.current);
          markersRef.current.push(marker);
          
 el.addEventListener('click', () => {
        const currentBearing = mapRef.current.getBearing();
      const currentZoom = mapRef.current.getZoom();

    
        
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

      });
 
      const pinShapeEl = el.querySelector('.pinShape');
  pinShapeEl.classList.add('pulse-marker');

 

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

    // fly to the selected location
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

    // find and open its popup
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


      console.log("Mapbox token:", process.env.NEXT_PUBLIC_MAPBOX_TOKEN);
    return (
        <>
          { /*defining reset button to adjust the map coordinates to its original state when clicked */}
            <button className='reset-button' onClick={handleButtonClick}>
                Reset
            </button>
            <div id='map-container' ref={mapContainerRef}>
             
            </div>
        </>
    )
}
