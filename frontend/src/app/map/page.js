'use client';
import dynamic from 'next/dynamic';


const Map = dynamic(() => import('@/app/components/map'),{ ssr: false });
const SideBar =dynamic(() => import('@/app/components/sidebar'),{ ssr: false });

export default function MapPage() {
  return (
    
    <main className="map-layout">
      <SideBar />
      <Map />
    </main>
  );
}
