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
export default function Map(){

    const mapRef =useRef()
    const mapContainerRef= useRef()

    const [center, setCenter]=useState(INITIAL_CENTER)
    const [zoom,setZoom]=useState(INITIAL_ZOOM)

    useEffect(() => {
    if (!useMapbox) return;

    if (mapRef.current) return;
    
    mapRef.current = new mapboxgl.Map({
      container: mapContainerRef.current,
      center: center,
      zoom: zoom,
    });

    mapRef.current.on('move', () => {
      const mapCenter = mapRef.current.getCenter()
      const mapZoom = mapRef.current.getZoom()

      setCenter([ mapCenter.lng, mapCenter.lat ])
      setZoom(mapZoom)
    })
    return () => {
      mapRef.current.remove()
    }
  }, [])

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
  mapRef.current.flyTo({
    center: INITIAL_CENTER,
    zoom: INITIAL_ZOOM
  })
}
    return (
        <>
          //defining reset button to adjust the map coordinates to its original state when clicked
            <button className='reset-button' onClick={handleButtonClick}>
                Reset
            </button>
            <div id='map-container' ref={mapContainerRef}>
               <div className="map-info-card flex">
                  <div className='map-info activity'>
                  <hr />
                  <div className='activity-container flex'>
                  </div>
                  </div>
                  <div className='map-info time'>
                  <hr></hr>
                  </div>
               </div>
            </div>
        </>
    )
}
