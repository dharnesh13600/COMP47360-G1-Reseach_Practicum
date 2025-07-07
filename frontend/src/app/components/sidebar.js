'use client';
import { useEffect, useRef, useState } from "react";
import ActivityLetters from '@/helper/activity';
import DateLetters from "@/helper/date";
import TimeLetters from '@/helper/time';
import styles from '../styles/sidebar.module.css';
import '../globals.css';


// importing dropdown components
import Dropdown from './dropdowns/actDropdown';
import DropdownItem from "@/helper/activityItem.js";




export default function SideBar(){
    const [selected, setSelected] = useState('2');

    // activity dropdown useState
    const [activityChoice, setChoice]=useState("Select...");

    // activity items 
    const activities=["Portrait Photography","Street Photography","Landscape Painting","Portrait Painting","Art Sale","Busking","Filmmaking"];

    // to open and close the dropdowns
    const [isOpen, setIsOpen]=useState(false);
    const dropRef= useRef();


    // setting visibility for locations container
const [isVisible, setIsVisible]=useState(false);
const handleToggleClick=()=>{
  console.log('Button clicked!');
  setIsVisible(prev => !prev);
};

// defining zones 
const manhattanNeighborhoods = {
  "financial district": [
    "Financial District North",
    "Financial District South",
    "World Trade Center",
    "Battery Park",
    "Battery Park City",
    "Seaport",
    "TriBeCa/Civic Center"
  ],
  "soho hudson square": [
    "SoHo",
    "Hudson Sq",
    "Little Italy/NoLiTa"
  ],
  "lower east side": [
    "Lower East Side",
    "Chinatown",
    "Two Bridges/Seward Park"
  ],
  "east village": [
    "East Village",
    "Alphabet City"
  ],
  "west village": [
    "West Village",
    "Greenwich Village North",
    "Greenwich Village South",
    "Meatpacking/West Village West"
  ],
  "chelsea": [
    "West Chelsea/Hudson Yards",
    "East Chelsea",
    "Flatiron"
  ],
  "midtown": [
    "Midtown South",
    "Midtown Center",
    "Midtown North"
  ],
  "times square theater district": [
    "Times Sq/Theatre District",
    "Garment District"
  ],
  "murray hill area": [
    "Murray Hill",
    "Kips Bay",
    "Gramercy"
  ],
  "union square area": [
    "Union Sq",
    "Stuy Town/Peter Cooper Village",
    "UN/Turtle Bay South",
    "Sutton Place/Turtle Bay North"
  ],
  "clinton hells kitchen": [
    "Clinton East",
    "Clinton West"
  ],
  "upper west side": [
    "Upper West Side South",
    "Upper West Side North",
    "Lincoln Square East",
    "Lincoln Square West",
    "Manhattan Valley",
    "Bloomingdale"
  ],
  "upper east side": [
    "Upper East Side South",
    "Upper East Side North",
    "Yorkville East",
    "Lenox Hill East",
    "Lenox Hill West"
  ],
  "central park": [
    "Central Park"
  ],
  "harlem": [
    "Central Harlem",
    "Central Harlem North",
    "Morningside Heights",
    "Manhattanville"
  ],
  "hamilton heights": [
    "Hamilton Heights"
  ],
  "east harlem": [
    "East Harlem South",
    "East Harlem North"
  ],
  "washington heights": [
    "Washington Heights South",
    "Washington Heights North"
  ],
  "inwood": [
    "Inwood",
    "Inwood Hill Park"
  ],
  "special areas": [
    "Roosevelt Island",
    "Randalls Island",
    "Marble Hill",
    "Highbridge Park",
    "Governor's Island/Ellis Island/Liberty Island"
  ]
};

// creating areas with only area names
const manhattanAreas=Object.keys(manhattanNeighborhoods);

const [locations, setLocations] = useState([]);
useEffect(() => {
  // Simulate loading JSON data
  const locationJson = {
    "locations": [
      {
        "id": "888a34ae-dcfd-4e2f-bfb5-43782c91aecd",
        "zoneName": "Washington Square Park: Arch Plaza",
        "latitude": 40.7312185,
        "longitude": -73.9970929,
        "combinedScore": 5.03,
        "activityScore": 4.14,
        "museScore": null,
        "crowdScore": null
      },
      {
        "id": "1947f53a-1b20-4c68-a06d-1606efac5aa5",
        "zoneName": "Bryant Park: Stage Performance",
        "latitude": 40.7548472,
        "longitude": -73.9841117,
        "combinedScore": 4.99,
        "activityScore": 4.30,
        "museScore": null,
        "crowdScore": null
      },
      {
        "id": "2cbf69e0-bc5c-4d89-8dda-c75bbc6c44f7",
        "zoneName": "WEST END AVENUE ",
        "latitude": 40.7883655,
        "longitude": -73.9745122,
        "combinedScore": 5.69,
        "activityScore": 4.36,
        "museScore": null,
        "crowdScore": null
      },
      {
        "id": "a262331c-8144-4c7b-b59b-06d21690c95d",
        "zoneName": "8 AVENUE Manhattan, New York",
        "latitude": 40.8164207,
        "longitude": -73.9466177,
        "combinedScore": 9.17,
        "activityScore": 8.88,
        "museScore": null,
        "crowdScore": null
      },
      {
        "id": "afb93f10-2dec-4acb-a048-ab5f8493903a",
        "zoneName": "FREDERICK DOUGLASS BOULEVARD",
        "latitude": 40.8164207,
        "longitude": -73.9466177,
        "combinedScore": 9.10,
        "activityScore": 8.74,
        "museScore": null,
        "crowdScore": null
      }
    ]
  };

  setLocations(locationJson.locations);
}, []);

    return(
          <>
          <div 
          className={styles['sidebarContainer']}
          >

                <div
                className={`${styles.sidebarInner} ${styles.activity}`}
                >
                    <hr/>
                    <div className={`${styles.innerPosition}`}>
                    <div>
                    <p>Choose Your</p>
                    <p><ActivityLetters/></p>
                    </div>
                    
                    </div>
                    
                      <div className={styles.dropdownWrapper}>
                      <Dropdown ref={dropRef} buttonText={activityChoice} content={ (close)=>(   
                        <>
                          {
                          activities.map((activity) => (
                          <DropdownItem 
                          key={activity}
                          onClick={()=>{setChoice(activity); close();}}>
                          {activity}
                          </DropdownItem>
                          ))
                          }
                        </>
    )}/>
                    </div>
                    
               
                </div>
                <div
                className={`${styles.sidebarInner} ${styles.time}`}
                >
                     <hr/>
                    <div className={`${styles.innerPosition}`}>
                    <div>
                    <p>Choose Your</p>
                     <div className={`${styles.dateContainer}`}>
                        <div><DateLetters/></div>
                        <div className={`${styles.vl}`}></div>
                         <div className={`${styles.timeDiv}`}><TimeLetters/></div>

                    </div>
                   
                    </div>
                 
                    </div>
                    <hr />

                </div>

                    <div className={styles.locationHeader}>
                    <div className={`${styles.recArea} ${isVisible ? styles.active : styles.inactive}`}>
                      <p className={styles.recommendation}>Recommended</p>
                      <p className={styles.area}>Area</p>
                    </div>
                      
                     <button onClick={handleToggleClick} className="areaToggleBtn">
                     {!isVisible && (
                      <svg width="40" height="25" viewBox="0 0 40 48" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M32 10H16C8.26801 10 2 16.268 2 24C2 31.732 8.26801 38 16 38H32C39.732 38 46 31.732 46 24C46 16.268 39.732 10 32 10Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
<path d="M16 30C19.3137 30 22 27.3137 22 24C22 20.6863 19.3137 18 16 18C12.6863 18 10 20.6863 10 24C10 27.3137 12.6863 30 16 30Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
</svg>
                     )} 
                     {isVisible && (
                      <svg  width="40" height="25" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M32 9.99902H16C8.26801 9.99902 2 16.267 2 23.999C2 31.731 8.26801 37.999 16 37.999H32C39.732 37.999 46 31.731 46 23.999C46 16.267 39.732 9.99902 32 9.99902Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
<path d="M32 29.999C35.3137 29.999 38 27.3127 38 23.999C38 20.6853 35.3137 17.999 32 17.999C28.6863 17.999 26 20.6853 26 23.999C26 27.3127 28.6863 29.999 32 29.999Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
</svg>

                     )}
                      </button> 
<span className={`${styles.manualSelection} ${isVisible ? styles.inactive  :styles.active}`}>Select Area</span>
                  </div>
                 {!isVisible && (
                   <div className={styles.locationListContainer}>
  {locations.map((location,index) => (
    <div key={location.id} className={styles.locationItem}>
      <span className={styles.index}>{index+1}</span><span className={styles.locationName}>{location.zoneName}</span>
    </div>
  ))}
</div>
                 )} 
                 {isVisible && (
                  <div className={styles.suggestedLocations}>
                      <span>Select Area</span>
                       <div className={styles.suggestedItems}>
                        {Object.keys(manhattanNeighborhoods).map(area => (
      <div key={area} className={styles.suggestedAreas}>
        {area}
      </div>
    ))}
                       
                       </div>
                        
             
                    </div>
                 )}
          </div>
    </>
    );

}