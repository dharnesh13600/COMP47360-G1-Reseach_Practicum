'use client';
import { useState } from 'react';
import dynamic from 'next/dynamic';
import MapDraw from '../components/map-draw-1.js'
import MapDraw02 from '../components/map-draw-2.js'
import '../styles/map-draw-1.css'
import '../styles/map-draw-2.css'

const Map = dynamic(() => import('@/app/components/map'),{ ssr: false });
const SideBar =dynamic(() => import('@/app/components/sidebar'),{ ssr: false });



export default function MapPage() {
const [submitted, setSubmitted] = useState(false);  
 const [locations, setLocations] = useState([]);

const [selectedLocation, setSelectedLocation] = useState(null);
  return (
    
    <main className="map-layout">
      <SideBar  onSubmit={(locs) => {
          setSubmitted(true);
          setLocations(locs);
        }} onLocationSelect ={setSelectedLocation}/>
      <Map submitted={submitted}
        locations={locations} selectedLocation={selectedLocation}/>
    </main>
  );
}