'use client';
import { useState } from 'react';
import dynamic from 'next/dynamic';
import MapDraw from '../components/map-draw-1.js'
import MapDraw02 from '../components/map-draw-2.js'
import '../styles/map-draw-1.css'
import '../styles/map-draw-2.css'

const Map = dynamic(() => import('@/app/components/map'),{ ssr: false });
const SideBar =dynamic(() => import('@/app/components/sidebar'),{ ssr: false });

  const locationJson = {
  "locations": [
    {
      "id": "888a34ae-dcfd-4e2f-bfb5-43782c91aecd",
      "zoneName": "Washington Square Park",
      "latitude": 40.7312185,
      "longitude": -73.9970929,
      "combinedScore": 5.03,
      "activityScore": 4.14,
      "museScore": null,
      "crowdScore": null
    },
    {
      "id": "1947f53a-1b20-4c68-a06d-1606efac5aa5",
      "zoneName": "Bryant Park",
      "latitude": 40.7548472,
      "longitude": -73.9841117,
      "combinedScore": 4.99,
      "activityScore": 4.30,
      "museScore": null,
      "crowdScore": null
    },
    {
      "id": "2cbf69e0-bc5c-4d89-8dda-c75bbc6c44f7",
      "zoneName": "West End Avenue",
      "latitude": 40.7883655,
      "longitude": -73.9745122,
      "combinedScore": 5.69,
      "activityScore": 4.36,
      "museScore": null,
      "crowdScore": null
    },
    {
      "id": "a262331c-8144-4c7b-b59b-06d21690c95d",
      "zoneName": "8th Avenue",
      "latitude": 40.763826,
      "longitude": -73.982222,
      "combinedScore": 9.17,
      "activityScore": 8.88,
      "museScore": null,
      "crowdScore": null
    },
    {
      "id": "afb93f10-2dec-4acb-a048-ab5f8493903a",
      "zoneName": "Frederick Douglass",
      "latitude": 40.810000,
      "longitude": -73.950000,
      "combinedScore": 9.10,
      "activityScore": 8.74,
      "museScore": null,
      "crowdScore": null
    },
    {
      "id": "ecdfb7fa-46c7-4f0d-9f3e-22ee9e8d4567",
      "zoneName": "Central Park North",
      "latitude": 40.800679,
      "longitude": -73.958248,
      "combinedScore": 7.85,
      "activityScore": 7.12,
      "museScore": null,
      "crowdScore": null
    },
    {
      "id": "d6a08f7e-cc5c-4e1e-913a-3f3a907c7fd9",
      "zoneName": "South Street Seaport",
      "latitude": 40.706917,
      "longitude": -74.003638,
      "combinedScore": 6.42,
      "activityScore": 6.00,
      "museScore": null,
      "crowdScore": null
    }
  ]
};

export default function MapPage() {
const [submitted, setSubmitted] = useState(false);  
 const [locations, setLocations] = useState([]);

const [selectedLocation, setSelectedLocation] = useState(null);
const [showAllLocations, setShowAllLocations] = useState(false);
const visibleLocations =
  !showAllLocations ? locations.slice(0, 5) : locations;

const [selectedTime, setSelectedTime] = useState(null);

  return (
    
    <main className="map-layout">
      <SideBar  
      locations={locations}
   visibleLocations={visibleLocations}
   showAllLocations={showAllLocations}
   setShowAllLocations={setShowAllLocations}
   onSelectedTimeChange={setSelectedTime}
      onSubmit={() => {
          setSubmitted(true);
          setLocations(locationJson.locations);
        }} onLocationSelect ={setSelectedLocation}/>
      <Map submitted={submitted}
        locations={visibleLocations} selectedLocation={selectedLocation} selectedTime={selectedTime}/>
    </main>
  );
}



