use client';
import dynamic from 'next/dynamic';
import MapDraw from '../components/map-draw-1.js'
import MapDraw02 from '../components/map-draw-2.js'
import '../styles/map-draw-1.css'
import '../styles/map-draw-2.css'

const Map = dynamic(() => import('@/app/components/map'),{ ssr: false });
const SideBar =dynamic(() => import('@/app/components/sidebar'),{ ssr: false });

export default function MapPage() {
  return (
    
    <main className="map-layout">
      <MapDraw />
      <MapDraw02 />
      <SideBar />
      <Map />
    </main>
  );
}