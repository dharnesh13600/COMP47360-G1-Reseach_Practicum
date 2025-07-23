// 'use client';
// import { useState,useEffect } from 'react';
// import dynamic from 'next/dynamic';
// import MapDraw from '../components/map-draw-1.js'
// import MapDraw02 from '../components/map-draw-2.js'
// import '../styles/map-draw-1.css'
// import '../styles/map-draw-2.css'


// // import { fetchZoneCoords } from '../api/fetchZoneCoords/route.js';

// const Map = dynamic(() => import('@/app/components/map'),{ ssr: false });
// const SideBar =dynamic(() => import('@/app/components/sidebar'),{ ssr: false });

// const zoneJson={
//   "locations": [
//     {
//       "id": "4f6def91-c56e-45db-85af-9d9053c17da1",
//       "zoneName": "46/47 Broadway Pedestrian Plaza (Times Sq)",
//       "latitude": 40.7586759,
//       "longitude": -73.9862195,
//       "activityScore": 5.433528900146484,
//       "museScore": 10.0,
//       "crowdScore": 10.0,
//       "estimatedCrowdNumber": 33962,
//       "crowdLevel": "Quiet",
//       "scoreBreakdown": {
//         "activityScore": 5.433528900146484,
//         "museScore": 5.1,
//         "crowdScore": 5.009671688079834,
//         "explanation": "Calculated from ML .pkl file"
//       }
//     },
//     {
//       "id": "1947f53a-1b20-4c68-a06d-1606efac5aa5",
//       "zoneName": "Bryant Park: Stage Performance",
//       "latitude": 40.7548472,
//       "longitude": -73.9841117,
//       "activityScore": 4.218434810638428,
//       "museScore": 9.8,
//       "crowdScore": 10.0,
//       "estimatedCrowdNumber": 36752,
//       "crowdLevel": "Busy",
//       "scoreBreakdown": {
//         "activityScore": 4.218434810638428,
//         "museScore": 4.9,
//         "crowdScore": 5.150505542755127,
//         "explanation": "Calculated from ML .pkl file"
//       }
//     },
//     {
//       "id": "d0ea7dfe-96f5-4b23-b6b0-993d854dc26e",
//       "zoneName": "LIBERTY STREET between CHURCH STREET and BROADWAY",
//       "latitude": 40.7594328,
//       "longitude": -73.9851453,
//       "activityScore": 4.299066543579102,
//       "museScore": 9.8,
//       "crowdScore": 10.0,
//       "estimatedCrowdNumber": 35739,
//       "crowdLevel": "Busy",
//       "scoreBreakdown": {
//         "activityScore": 4.299066543579102,
//         "museScore": 4.9,
//         "crowdScore": 5.097929954528809,
//         "explanation": "Calculated from ML .pkl file"
//       }
//     },
//     {
//       "id": "bed5ec05-40fb-4316-834f-e509e4c0f9db",
//       "zoneName": "WEST 36 STREET Manhattan, New York",
//       "latitude": 40.7534731,
//       "longitude": -73.9925090,
//       "activityScore": 4.2702765464782715,
//       "museScore": 6.2,
//       "crowdScore": 5.126136779785156,
//       "estimatedCrowdNumber": 16960,
//       "crowdLevel": "Quiet",
//       "scoreBreakdown": {
//         "activityScore": 4.2702765464782715,
//         "museScore": 3.1,
//         "crowdScore": 2.563068389892578,
//         "explanation": "Calculated from ML .pkl file"
//       }
//     }
//   ],
// }

// export default function MapPage() {
// const [submitted, setSubmitted] = useState(false);  
// const [submittedLocations,setSubmittedLocations]=useState([]);
// const [zoneLocations,setZoneLocations]=useState([]);

// const [selectedLocation, setSelectedLocation] = useState(null);
// const [showAllLocations, setShowAllLocations] = useState(false);
// const visibleLocations =
//   !showAllLocations ? submittedLocations.slice(0, 5) : submittedLocations;
// const [activeSet, setActiveSet] = useState('submitted');
// const safeLocations = Array.isArray(
//   activeSet === 'submitted' ? submittedLocations : zoneLocations
// ) ? (activeSet === 'submitted' ? submittedLocations : zoneLocations) : [];

// const [zones,setZones]=useState([]);
// const [selectedZone,setSelectedZone]=useState(null);

// const [selectedTime, setSelectedTime] = useState(null);


//  const [showLocations, setShowLocations] = useState(true);

//   const handleZoneResults = (data, area) => {
//     setZoneLocations(data.locations);
//     setShowLocations(false);        // switch to zone markers
//   };

//   return (
    
//     <main className="map-layout">
//       <SideBar  
//       locations={submittedLocations}
//    visibleLocations={visibleLocations}
//    showAllLocations={showAllLocations}
//    zones={zones}
//    setShowAllLocations={setShowAllLocations}
//    onSelectedTimeChange={setSelectedTime}
//       onSubmit={() => {
//         setSubmitted(true);
//         setSubmittedLocations(submittedLocations);
//         setActiveSet('submitted');
//         setShowLocations(true); 
          
//         }} onLocationSelect ={setSelectedLocation} onZoneResults={(zoneJson, area) => {
//         setSelectedZone(area);
//         setZoneLocations(zoneJson.locations);
//         setActiveSet('zone');
//       }}/>
//       <Map submitted={submitted}
//         locations={submittedLocations} selectedLocation={selectedLocation} selectedTime={selectedTime} selectedZone={selectedZone} zoneLocations={zoneLocations} showLocations={showLocations}/>
//     </main>
//   );
// }

//------------------------------------------

// 'use client';
// import { useState } from 'react';
// import dynamic from 'next/dynamic';
// import '../styles/map-draw-1.css';
// import '../styles/map-draw-2.css';

// const Map = dynamic(() => import('@/app/components/map'), { ssr: false });
// const SideBar = dynamic(() => import('@/app/components/sidebar'), { ssr: false });

// export default function MapPage() {
//   const [submitted, setSubmitted] = useState(false);
//   const [submittedLocations, setSubmittedLocations] = useState([]);
//   const [zoneLocations, setZoneLocations] = useState([]);
//   const [selectedLocation, setSelectedLocation] = useState(null);
//   const [showAllLocations, setShowAllLocations] = useState(false);
//   const [activeSet, setActiveSet] = useState('submitted');
//   const [zones, setZones] = useState([]);
//   const [selectedZone, setSelectedZone] = useState(null);
//   const [selectedTime, setSelectedTime] = useState(null);
//   const [showLocations, setShowLocations] = useState(true);

//   const visibleLocations =
//     !showAllLocations ? submittedLocations.slice(0, 5) : submittedLocations;

//   const handleZoneResults = (data, area) => {
//     setZoneLocations(data.locations);
//     setShowLocations(false); // switch to zone markers
//   };

//   return (
//     <main className="map-layout">
//       <SideBar
//         locations={submittedLocations}
//         visibleLocations={visibleLocations}
//         showAllLocations={showAllLocations}
//         zones={zones}
//         setShowAllLocations={setShowAllLocations}
//         onSelectedTimeChange={setSelectedTime}
//         onSubmit={(newLocations) => {
//           setSubmitted(true);
//           setSubmittedLocations(newLocations);
//           setActiveSet('submitted');
//           setShowLocations(true);
//         }}
//         onLocationSelect={setSelectedLocation}
//         onZoneResults={(zoneJson, area) => {
//           setSelectedZone(area);
//           setZoneLocations(zoneJson.locations);
//           setActiveSet('zone');
//         }}
//       />

//       <Map
//         submitted={submitted}
//         locations={submittedLocations}
//         selectedLocation={selectedLocation}
//         selectedTime={selectedTime}
//         selectedZone={selectedZone}
//         zoneLocations={zoneLocations}
//         showLocations={showLocations}
//       />
//     </main>
//   );
// }




'use client';
import { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';
import '../styles/map-draw-1.css';
import '../styles/map-draw-2.css';

const Map = dynamic(() => import('@/app/components/map'), { ssr: false });
const SideBar = dynamic(() => import('@/app/components/sidebar'), { ssr: false });

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
  // Sidebar visible list
  const visibleLocations = !showAllLocations
    ? submittedLocations.slice(0, 5)
    : submittedLocations.slice(0,7);

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
        onSelectedTimeChange={() => {}}
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
        locations={submittedLocations}
        selectedLocation={selectedLocation}
        selectedTime={null}         
        selectedZone={selectedZone}
        zoneLocations={zoneLocations}
        showLocations={showLocations}
        clearMarkers={clearMarkers}
         showAllLocations={showAllLocations}
      />
    </main>
  );
}
