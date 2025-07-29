'use client';
import { useState, useEffect,useMemo } from 'react';
import dynamic from 'next/dynamic';
import '../../components/mapComponent/map-draw-1.css';
import '../../components/mapComponent/map-draw-2.css';
const Map = dynamic(() => import('@/app/components/mapComponent/map'), { ssr: false });
const SideBar = dynamic(() => import('@/app/components/sidebar/sidebar'), { ssr: false });

export default function MapPage() {
  const [submitted, setSubmitted] = useState(false);
  const [submittedLocations, setSubmittedLocations] = useState([]);
  const [zoneLocations, setZoneLocations] = useState([]);
  const [selectedLocation, setSelectedLocation] = useState(null);
  const [selectedZone, setSelectedZone] = useState(null);
  const [showAllLocations, setShowAllLocations] = useState(false);

  // Toggle which set of markers to show
  const [showLocations, setShowLocations] = useState(true);
const [clearMarkers, setClearMarkers] = useState(false);
const [selectedTime, setSelectedTime] = useState(null);
  // Sidebar visible list
const visibleLocations = useMemo(() => {
  return !showAllLocations
    ? submittedLocations.slice(0, 5)
    : submittedLocations.slice(0, 10);
}, [showAllLocations, submittedLocations]);

  // Handle activity/date/time submission
  const handleSubmit = (locations) => {
    setSubmitted(true);
    setSubmittedLocations(locations);
    setShowLocations(true);
  };

  // Handle zone click and results from Sidebar
  const handleZoneResults = (data, area) => {
    setSelectedZone(area);
    setZoneLocations(data.locations);
    setShowLocations(false);
  };

  useEffect(() => {
  if (clearMarkers) {

    setClearMarkers(false);
  }
}, [clearMarkers]);
  return (
    <main className="map-layout">
      <SideBar
        locations={submittedLocations}
        visibleLocations={visibleLocations}
        showAllLocations={showAllLocations}
        setShowAllLocations={setShowAllLocations}
        showLocations={showLocations}
       setShowLocations={setShowLocations}
        clearMarkers={clearMarkers}
       setClearMarkers={setClearMarkers}
        zones={[]}               
         onSelectedTimeChange={setSelectedTime}
        onSubmit={handleSubmit}
        onLocationSelect={setSelectedLocation}
      onZoneResults={(zoneJson, areaName) => {
    
    setZoneLocations(zoneJson.locations);
  const match =
    zoneJson.locations.find(z => z.zoneName.includes(areaName))
    || zoneJson.locations[0];
    setSelectedZone(match);
  }}
      />

      <Map
        submitted={submitted}
        locations={visibleLocations}
        selectedLocation={selectedLocation}
        selectedTime={selectedTime}         
        selectedZone={selectedZone}
        zoneLocations={zoneLocations}
        showLocations={showLocations}
        clearMarkers={clearMarkers}
         showAllLocations={showAllLocations}
      />
    </main>
  );
}