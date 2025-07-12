'use client';
import { useEffect, useRef, useState } from "react";
import styles from '../styles/sidebar.module.css';
import Image from'next/image';
import {parse,format} from 'date-fns';
import {AiOutlineClose} from 'react-icons/ai';

// importing dropdown components
import Dropdown from './dropdowns/actDropdown';
import DropdownItem from "@/helper/activityItem.js";

import DropdownDate from "./dropdowns/dateDropdown.js";
import DateItem from "@/helper/dateItem.js";

import DropdownTime from "./dropdowns/timeDropdown";
import TimeItem from "@/helper/timeItem";

import { GetWeatherData } from "./weather-data";



import { useWeather } from "./useWeather";
export default function SideBar(){


    const [selected, setSelected] = useState('2');

    // activity dropdown useState
    const [activityChoice, setChoice]=useState(null);

    // activity items 
    const activities=["Portrait Photography","Street Photography","Landscape Painting","Portrait Painting","Art Sale","Busking","Filmmaking"];

    // to open and close the dropdowns
    const [isOpen, setIsOpen]=useState(false);
    const dropRef= useRef();
 const [weatherItems, setweatherItems] = useState([]);
    const [fullWeatherData, setFullWeatherData] = useState([]);

    const [timeItems, settimeItems] = useState([]);
    const [isDateOpen,setIsDateOpen]=useState([]);
    const [isTimeOpen, setIsTimeOpen] = useState(false);
    const [selectedDate, setSelectedDate] = useState(null);
    const [selectedTime, setSelectedTime] = useState(null);
    const [submitted, setSubmitted] = useState(false);


const [dates,setDates]=useState([]);
const [times,setTimes]=useState([]);
const [weather,setWeather]=useState(null);


useEffect(()=>{
    async function weatherFetch(){
    const dateSet=new Set();
    const weatherResponse =await GetWeatherData();
    setFullWeatherData(weatherResponse.list);
    weatherResponse.list.forEach(entry=>{
            const dateObj=new Date(entry.readableTime);

            const formattedDate= format(dateObj,'MMMM d');
            dateSet.add(formattedDate);
            


        });
        const dates=Array.from(dateSet);
        setDates(dates);
        console.log("weatherResponse", weatherResponse);
console.log("weatherResponse.list", weatherResponse.list);

    }
    weatherFetch();
},[]);

// updating time when date changes
useEffect(()=>{
  if(!selectedDate){
    setTimes([]);
    return;
  }

  const matchingTimes=new Set();

  fullWeatherData.forEach(entry=>{
    const readableDate= new Date(entry.readableTime);
    const dateStr=format(readableDate,'MMMM d');
    const timeStr=format(readableDate,'h:mm a');

    if (dateStr==selectedDate){
      const timeStr=format(readableDate, 'h:mm a');
      matchingTimes.add(timeStr);
    }
  });
  const times=Array.from(matchingTimes);
  setTimes(times);
},[selectedDate,fullWeatherData]);
useEffect(()=>{
    if(!submitted){
      setWeather(null);
      return;
    }
    if(!selectedDate || !selectedTime){
    setWeather(null);
    return;
  }

  const matches= fullWeatherData.find(entry=>{
    const readableObj=new Date(entry.readableTime);
    const dateStr=format(readableObj,'MMMM d');
    const timeStr=format(readableObj,'h:mm a');

    return dateStr== selectedDate && timeStr==selectedTime;
  });
  if(matches){
    setWeather({
      condition:matches.condition,
      temp:matches.temp
    });
  }
  else{
    setWeather(null);
  }

 
  
},[submitted,selectedDate,selectedTime,fullWeatherData]);

const {icon,temp}=useWeather(setWeather || {});





useEffect(() => {
  // Simulate loading JSON data
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
        "zoneName": "WEST END AVENUE",
        "latitude": 40.7883655,
        "longitude": -73.9745122,
        "combinedScore": 5.69,
        "activityScore": 4.36,
        "museScore": null,
        "crowdScore": null
      },
      {
        "id": "a262331c-8144-4c7b-b59b-06d21690c95d",
        "zoneName": "8 AVENUE",
        "latitude": 40.8164207,
        "longitude": -73.9466177,
        "combinedScore": 9.17,
        "activityScore": 8.88,
        "museScore": null,
        "crowdScore": null
      },
      {
        "id": "afb93f10-2dec-4acb-a048-ab5f8493903a",
        "zoneName": "FREDERICK DOUGLASS",
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

const [isVisible, setIsVisible]=useState(false);
const handleToggleClick=()=>{
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

async function handleSubmit(){
  if(!activityChoice || !selectedTime){
    alert("Please select both activity and time");
    return;
  }
  setSubmitted(true);

  const date = parse(`${selectedDate} ${selectedTime}`, 'MMMM d h:mm a', new Date());
const readableTimeJson = format(date, 'yyyy-MM-dd HH:mm a');
  const res=await fetch('/api/location',{
    method:'POST',
    headers:{
      'Content-Type':'application/json'
    },
    body:JSON.stringify({activity:activityChoice,readableTime:readableTimeJson})
  });

  const data=await res.json();

  if(res.ok){
    console.log("Success:",data);
  }
  else{
    console.error("Error:",data.error);
  }

};
const [locations, setLocations] = useState([]);
useEffect(() => {
  // Simulate loading JSON data
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
        "zoneName": "8 AVENUE",
        "latitude": 40.8164207,
        "longitude": -73.9466177,
        "combinedScore": 9.17,
        "activityScore": 8.88,
        "museScore": null,
        "crowdScore": null
      },
      {
        "id": "afb93f10-2dec-4acb-a048-ab5f8493903a",
        "zoneName": "FREDERICK DOUGLASS ",
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
useEffect(() => {
  console.log("selectedDate updated:", selectedDate);
}, [selectedDate]);
useEffect(() => {
  console.log("Dates available:", dates);
}, [dates]);

    return(
          <>
          <div 
          className={styles['sidebarContainer']}
          >

                <div
                className={styles.activityWrapper}
                > 
                   
                      <Dropdown ref={dropRef} 
                      buttonText={<span>
                          {activityChoice ||'Select Activity'}
                      
                      {
                        activityChoice &&(
                          <AiOutlineClose
                          size={16}
                          onClick={(e)=>{
                            e.stopPropagation();
                            setChoice(null);
                          }}
                          className={styles.clearIcon}
                          />
                        )}
                        </span>
                   
                      }
                      activityChoice={activityChoice}
                        content={ (close)=>(   
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
                <div className={`${styles.readableTimeContainer}`}>
                        <div
                className={styles.dateWrapper}
                >
                    <div className={`${styles.innerPosition}`}>
                    <div>
                    
                     
                    
                     
                      <DropdownDate buttonText={ <span className={styles.buttonTextWrapper}>{selectedDate || "Date"}
                      {selectedDate && (
                        <AiOutlineClose
                          size={16}
                          onClick={(e)=>{
                            e.stopPropagation();
                            setSelectedDate(null);
                          }}
                          className={`${styles.clearIcon} `}
                          />
                      )}
                      </span>}
                      selectedDate={selectedDate}
                      content={(close)=>(<>
                        {dates.map(date=>{
                          console.log("Rendering DateItem:", {
    date,
    selectedDate,
    isSelected: date === selectedDate
  });
  return(
<DateItem key={date} onClick={()=>{setSelectedDate(date);requestAnimationFrame(close);}} className={date === selectedDate ? styles.selectedItem : ''}>
                            {date}
                            </DateItem>
  );
                          
                        }

                        )}
                        </>)} />
           
                   
                    
                    </div>
                 
                    </div>
                    

                </div>
                <div  className={styles.timeWrapper}>
                             <DropdownTime buttonText={<span className={styles.buttonTextWrapper}>{selectedTime || "Time"}{selectedTime && (
                        <AiOutlineClose
                          size={16}
                          onClick={(e)=>{
                            e.stopPropagation();
                            setSelectedTime(null);
                          }}
                          className={styles.clearIcon}
                          />
                      )}</span>} 
                      selectedTime={selectedTime}
                      content={<>
              {times.length === 0 && <div className={styles.dateEmptyText}>Select date</div>}
              {times.map(time=>(
                <TimeItem key={time} onClick={()=>{setSelectedTime(time);close();}}>
                  {time}
                </TimeItem>
              ))}
              </>} /> 
                </div>
                </div>
                
                <button className={styles.buttonStyle} onClick={handleSubmit}>Submit</button>
                {submitted && weather && (
  <div className={`${styles.weatherDisplay}  ${submitted ? 'fade-enter-active' : 'fade-enter'}`}>
    <img
      src={icon}
      alt={weather.condition}
      width={32}
      height={32}
      style={{ marginRight: '8px' }}
    />
    <span>{temp}°F</span>
  </div>
)}
                
                  
                    <div className={styles.locationHeader}>
                    <div className={`${styles.recArea} ${isVisible ? styles.active : styles.inactive}`}>
                      <p className={styles.recommendation}>Recommended</p>
                      <p className={styles.area}>Area</p>
                    </div>
                      
                     <button onClick={handleToggleClick} className="areaToggleBtn">
                     {!isVisible && (
                        <svg  width="40" height="25" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M32 9.99902H16C8.26801 9.99902 2 16.267 2 23.999C2 31.731 8.26801 37.999 16 37.999H32C39.732 37.999 46 31.731 46 23.999C46 16.267 39.732 9.99902 32 9.99902Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
<path d="M32 29.999C35.3137 29.999 38 27.3127 38 23.999C38 20.6853 35.3137 17.999 32 17.999C28.6863 17.999 26 20.6853 26 23.999C26 27.3127 28.6863 29.999 32 29.999Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
</svg>
                     )} 
                     {isVisible && (
                   
 <svg width="40" height="25" viewBox="0 0 40 48" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M32 10H16C8.26801 10 2 16.268 2 24C2 31.732 8.26801 38 16 38H32C39.732 38 46 31.732 46 24C46 16.268 39.732 10 32 10Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
<path d="M16 30C19.3137 30 22 27.3137 22 24C22 20.6863 19.3137 18 16 18C12.6863 18 10 20.6863 10 24C10 27.3137 12.6863 30 16 30Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
</svg>

                     )}
                      </button> 
<span className={`${styles.manualSelection} ${isVisible ? styles.inactive  :styles.active}`}>Select Area</span>
                  </div>
                {!submitted && isVisible && (
                     <div className={styles.noRecommendations}>
  Please submit your choices to view the recommended areas
</div>
                )}
                 {submitted && isVisible && (
                   <div className={styles.locationListContainer}>
  {locations.map((location,index) => (
    <div key={location.id} className={styles.locationItem}>
      <span className={styles.index}>{index+1}</span><span className={styles.locationName}>{location.zoneName}</span>
      <span><Image className='photo' src='/search.png' alt='d' width={30} height={25}/></span>
    </div>
  ))}
</div>
                 )} 
                 {!isVisible && (
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