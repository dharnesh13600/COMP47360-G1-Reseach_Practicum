'use client';
import dynamic from 'next/dynamic';


const About= dynamic(() => import('@/app/components/about'),{ ssr: false });

export default function MapPage() {
  return (
    <main className="map-layout">
      <About />
    </main>
  );
}
