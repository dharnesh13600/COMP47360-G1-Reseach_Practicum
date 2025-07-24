


'use client';
import { useEffect, useRef, useState } from "react";
import styles from '../styles/sidebar.module.css';
import Image from'next/image';
import {parse,format} from 'date-fns';
import {AiOutlineClose} from 'react-icons/ai';
import { FaCheck } from 'react-icons/fa';

// importing dropdown components
import Dropdown from './dropdowns/actDropdown';
import DropdownItem from "@/helper/activityItem.js";

import DropdownDate from "./dropdowns/dateDropdown.js";
import DateItem from "@/helper/dateItem.js";

import DropdownTime from "./dropdowns/timeDropdown";
import TimeItem from "@/helper/timeItem";



import { useWeather } from "./useWeather";

export default function SideBar({ 
  onLocationSelect, 
  onSubmit,
  locations,
  visibleLocations,
  showAllLocations,
  setShowAllLocations,
  zones,
  onSelectedTimeChange,
  onZoneResults, 
  showLocations, 
  setShowLocations,
  clearMarkers,
  setClearMarkers
}){
  console.log('SideBar component mounted/re-rendered');

// useStates for retrieving from api endpoints
const [activities, setActivities] = useState([]);
const [manhattanNeighborhoods, setManhattan]=useState([]);
const [dates,setDate]=useState([]);
const [times,setTime]=useState([]);
const [weather,setWeather]=useState(null);

const [selected, setSelected] = useState('2');

// activity dropdown useState
const [activityChoice, setChoice]=useState(null);

// to open and close the dropdowns
const [isOpen, setIsOpen]=useState(false);
const dropRef= useRef();
const [weatherItems, setweatherItems] = useState([]);

const [timeItems, settimeItems] = useState([]);
const [isDateOpen,setIsDateOpen]=useState([]);
const [isTimeOpen, setIsTimeOpen] = useState(false);
const [selectedDate, setSelectedDate] = useState(null);
const [selectedTime, setSelectedTime] = useState(null);
const [submitted, setSubmitted] = useState(false);

const [showMore,setShowMore]=useState(false);
const [visibleIndexes, setVisibleIndexes] = useState([]);
const [zone, setZone]=useState(null);
const [zonesLoaded, setZonesLoaded] = useState(false);

// Screen size detection
const [width, setWidth] = useState(typeof window !== "undefined" ? window.innerWidth : 900);

useEffect(() => {
  const handleResize = () => setWidth(window.innerWidth);
  if (typeof window !== "undefined") {
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }
}, []);

const isSmall = width < 629;
const isMedium = width >= 629 && width < 900;
const isLarge = width >= 900;
const isMobile = isSmall;
const isDesktop = isMedium || isLarge;

const today=new Date();

const isToday=(dateStr)=>{
  const todayStr=format(today, 'MMMM d');
  return dateStr===todayStr;
};

// State to control mobile location content visibility
const [showLocationContent, setShowLocationContent] = useState(false);

const handleToggleClick = () => {
  setClearMarkers(true);
  setShowLocations((prev) => !prev);
};

const handleLocationToggle = () => {
  setShowLocationContent((prev) => !prev);
};

function sanitizeName(str) {
  return str
    .replace(/[:,]/g, ' ')
    .replace(/\b(Manhattan|New York|And|Between)\b/gi, '')
    .replace(/\s{2,}/g, ' ')
    .trim();
}

function cleanAndTruncate(str, n = 3) {
  const cleaned = sanitizeName(str);
  const words = cleaned.split(/\s+/);
  return words.length <= n
    ? cleaned
    : words.slice(0, n).join(' ');
}

async function fetchActivities() {
  const res = await fetch('/api/fetchActivities');
  if (!res.ok) throw new Error('Failed to fetch activities');
  return res.json();
}

useEffect(() => {
  async function getActivities() {
    const data = await fetchActivities();
    setActivities(data);
  }
  getActivities();
}, []);

async function fetchZones() {
  const res = await fetch('/api/zones');
  if (!res.ok) throw new Error('Failed to fetch zones');
  return res.json();
}
useEffect(() => {
  if (zonesLoaded) return;

  async function getZones() {
    try {
      const data = await fetchZones();
      setManhattan(data);
      setZonesLoaded(true);
    } catch (err) {
      console.error('Error fetching zones:', err);
    }
  }

  getZones();
}, [zonesLoaded]);
async function fetchDateTimes() {
  const res = await fetch('/api/fetchDateTimes');
  if (!res.ok) throw new Error('Failed to fetch date-times');
  return res.json();
}

useEffect(()=>{
  async function getDate(){
    const data=await fetchDateTimes();
    const dateSet=new Set();
    data.forEach(entry =>{
      const dateObj=new Date(entry);
      const formattedDate= format(dateObj,'MMMM d');
      dateSet.add(formattedDate);
    });
    const dates=Array.from(dateSet);
    console.log("dates rendered",dates);
    setDate(dates);
  }
  getDate();
},[]);

useEffect(()=>{
  async function getTime(){
    const data=await fetchDateTimes();
    const timeSet=new Set();
    data.forEach(entry =>{
      const timeObj=new Date(entry);
      const timeStr=format(timeObj,'HH:mm ');
      timeSet.add(timeStr);
    });
    const times=Array.from(timeSet);
    setTime(times);
  }
  getTime();
},[]);

// displaying time only when date is clicked
useEffect(()=>{
  if(!selectedDate){
    setTime([]);
    return;
  }
  async function matchTime(){
    const data=await fetchDateTimes();
    const matchingTimes=new Set();
    data.forEach(entry=>{
      const readableDate= new Date(entry);
      const dateStr=format(readableDate,'MMMM d');
      const timeStr=format(readableDate,'HH:mm');
      if (dateStr==selectedDate){
        const timeStr=format(readableDate, 'HH:mm');
        matchingTimes.add(timeStr);
      }
    });
    const filteredTimes = Array.from(matchingTimes);
    setTime(filteredTimes);
  }
  matchTime();
},[selectedDate]);


async function fetchWeather(date, time) {
  const res = await fetch('/api/fetchWeather', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ selectedDate: date, selectedTime: time })
  });

  if (!res.ok) throw new Error('Failed to fetch weather');

  return res.json();
}
// retrieve weather info by passing selected date and selected time to getWeather
useEffect(()=>{
  if (!submitted || !selectedDate || !selectedTime){
    setWeather(null);
    return;
  }
  async function getWeather(){
    const data=await fetchWeather(selectedDate,selectedTime);
    if(data){
      setWeather(data);
    }
    else{
      setWeather(null);
    }
  }
  getWeather();
},[submitted,selectedDate,selectedTime]);




const {icon,temp}=useWeather(weather || {});

// setting the payload for post request to backend
async function handleSubmit(){
  if(!activityChoice.name || !selectedDate || !selectedTime){
    alert("Please select activity, date and time");
    return;
  }
  setSubmitted(true);
  try{
    const date = parse(`${selectedDate} ${selectedTime}`, 'MMMM d HH:mm', new Date());
    const readableTimeJson = format(date, "yyyy-MM-dd'T'HH:mm");

    const payload={
      activity:activityChoice.name,
      dateTime:readableTimeJson,
    };

    const res = await fetch('/api/fetchLocations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ activity: activityChoice.name, dateTime: readableTimeJson }),
    });
    
    if (!res.ok) {
      console.error('Fetch failed', await res.text());
      return;
    }
    
    const { locations } = await res.json();
    onSubmit(locations);   

    console.log("📬 Response Status:", res.status, res.statusText);
    console.log("📬 Response OK:", res.ok);
    console.log("📬 Response Headers:", [...res.headers.entries()]);

    if(!res.ok){
      console.error("HTTP Error:", res.status, res.statusText);
      return;
    }
  }
  catch(error){
    console.error("Error in handleSubmit:", error);
  }
};

async function handleZoneClick(area){
  if (!selectedDate || !selectedTime) {
    alert('Please select a date AND time before choosing a zone.');
    return;
  }
  setZone(area);

  try{
    const date = parse(`${selectedDate} ${selectedTime}`, 'MMMM d HH:mm', new Date());
    const readableTimeJson =format(date, "yyyy-MM-dd'T'HH:mm");
    console.log(activityChoice);

    const payload={
      activity:activityChoice.name,
      dateTime:readableTimeJson,
      selectedZone:area,
    }

    console.log("Submitting with zone: ",payload);

    const res=await fetch('/api/fetchLocations',{
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body:JSON.stringify(payload),
    });

    if(!res.ok){
      console.error("HTTP Error:", res.status, res.statusText);
      return;
    }

    const data=await res.json();
    if (!data) {
      console.error("Empty response received");
      return;
    }
    console.log("Zone success: ", data);
    onZoneResults?.(data, area);
  }
  catch (error) {
    console.error("Error in handleZoneClick:", error);
  }
};

useEffect(() => {
  if (submitted) {
    setVisibleIndexes([]);
    visibleLocations.forEach((_, i) => {
      let delay=200 +i*200;
      setTimeout(() => {
        setVisibleIndexes(prev => [...prev, i]);
      }, delay);
    });
  }
}, [submitted, visibleLocations]);

useEffect(() => {
  console.log("selectedDate updated:", selectedDate);
}, [selectedDate]);

useEffect(() => {
  console.log("Dates available:", dates);
}, [dates]);

useEffect(() => {
  setShowLocations(!isSmall);
}, [isSmall, setShowLocations]);

// Initialize showLocationContent based on screen size
useEffect(() => {
  if (isDesktop) {
    setShowLocationContent(true); // Always show content on desktop
  } else {
    setShowLocationContent(false); // Start collapsed on mobile
  }
}, [isDesktop]);

const maxItems = showAllLocations ? visibleLocations.length : 7;

// Common dropdown components
const ActivityDropdown = ({ className = "" }) => (
  <div className={`${styles.activityWrapper} ${className}`}>
    <Dropdown 
      ref={dropRef} 
      buttonText={
        <span className={`${styles.buttonTextWrapper} ${activityChoice ? styles.selectedItem : ''}`}>
          {activityChoice?.name || 'Select Activity'}
          {activityChoice && (
            <AiOutlineClose
              size={16}
              onClick={(e)=>{
                e.stopPropagation();
                setChoice(null);
                setSubmitted(false);
              }}
              className={styles.clearIcon}
            />
          )}
        </span>
      }
      activityChoice={activityChoice}
      content={(close)=>(   
        <>
          
          {activities.map(activity => (
  <DropdownItem
    key={activity.id}
    onClick={() => {
      setChoice(activity); 
      setSubmitted(false); 
      close();
    }}
    className={activityChoice?.id === activity.id ? styles.selectedItem : ''}
  >
    {activity.name}
  </DropdownItem>
))}
        </>
      )}
    />
  </div>
);

const DateDropdown = ({ className = "" }) => (
  <div className={`${styles.dateWrapper} ${className}`}>
    <DropdownDate 
      buttonText={
        <span className={`${styles.buttonTextWrapper} ${selectedDate ? styles.selectedItem : ''} ${styles.otherContent}`}>
          {selectedDate ? (isToday(selectedDate) ? 'Today' : selectedDate) : 'Date'}
          {selectedDate && (
            <AiOutlineClose
              size={16}
              onClick={(e)=>{
                e.stopPropagation();
                setSelectedDate(null);
                setSubmitted(false);
                setWeather(null);
              }}
              className={`${styles.clearIcon}`}
            />
          )}
        </span>
      }
      selectedDate={selectedDate}
      content={(close)=>(
        <>
          {dates.map(date=>(
            <DateItem 
              key={date} 
              onClick={()=>{setSelectedDate(date);requestAnimationFrame(close); setSubmitted(false);}} 
              className={date === selectedDate ? styles.selectedItem : ''}
            >
              {isToday(date) ? 'Today' : date}
            </DateItem>
          ))}
        </>
      )} 
    />
  </div>
);

const TimeDropdown = ({ className = "" }) => (
  <div className={`${styles.timeWrapper} ${className}`}>
    <DropdownTime 
      buttonText={
        <span className={`${styles.buttonTextWrapper} ${selectedTime ? styles.selectedItem : ''}`}>
          {selectedTime || "Time"}
          {selectedTime && (
            <AiOutlineClose
              size={16}
              onClick={(e)=>{
                e.stopPropagation();
                setSelectedTime(null);
                setSubmitted(false);
                setWeather(null);
              }}
              className={styles.clearIcon}
            />
          )}
        </span>
      } 
      selectedTime={selectedTime}
      content={(close)=>(
        <>
          {times.length === 0 && <div className={styles.dateEmptyText}>Select date</div>}
          {times.map(time=>(
            <TimeItem key={time} onClick={()=>{setSelectedTime(time);setSubmitted(false);close();}}>
              {time}
            </TimeItem>
          ))}
        </>
      )} 
    />
  </div>
);

const WeatherDisplay = ({ className = "" }) => (
  <div className={`${styles.weatherDisplay} ${className} ${submitted && weather ? styles.show : ''}`}>
    {weather && (
      <>
        <Image
          src={icon}
          alt={weather.condition}
          width={32}
          height={32}
          style={{ marginRight: '8px' }}
        />
        <span>{temp}°F</span>
      </>
    )}
  </div>
);

const LocationsList = () => (
 <div className={`${styles.locationListContainer} ${showAllLocations ? styles.expanded : ''}`}>
    {visibleLocations.slice(0, maxItems).map((location,index) => (
      <div 
        key={location.id} 
        className={`${styles.locationItem} ${visibleIndexes.includes(index) ? styles.show : ''}`} 
        onClick={()=>onLocationSelect(location)}
      >
        <span className={styles.index}>{index+1}</span>
        <span className={styles.locationName}>
          {cleanAndTruncate(location.zoneName, 3)}
        </span>
        <span>
          <Image className='photo' src='/search.png' alt='d' width={30} height={25}/>
        </span>
      </div>
    ))}
    {locations.length > 5 && isLarge && (
      <button onClick={()=>setShowAllLocations(prev =>!prev)} className={styles.showMoreBtn}>
        {showAllLocations ? (
          <svg width="20" height="13" viewBox="0 0 25 13" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path opacity="0.9" d="M22.5 10.1353L12.3261 2.9999L3 10.1353" stroke="#177371" strokeWidth="4.5" strokeLinecap="round"/>
          </svg>
        ) : (
          <svg width="20" height="13" viewBox="0 0 25 13" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path opacity="0.9" d="M3 2.93237L13.1739 10.0677L22.5 2.93237" stroke="#177371" strokeWidth="4.5" strokeLinecap="round"/>
          </svg>
        )}
      </button>
    )}
  </div>
);

const ZoneSelection = () => (
  <div className={styles.suggestedLocations}>
    <span>Select Area</span>
    <div className={`${styles.suggestedItems}${isSmall && showAllLocations ? styles.compact : ''}`}>
      {/* {manhattanNeighborhoods.map(area => {
        console.log("Rendering area:", area);
        return (
          <div
            key={area}
            className={styles.suggestedAreas}
            onClick={() => {
              handleZoneClick(area);
            }}
          >
            {area}
          </div>
        );
      })} */}
     {manhattanNeighborhoods.map(area => {
  console.log(typeof area, area); 

  return (
    <div
      key={area}
      className={styles.suggestedAreas}
      onClick={() => {
        handleZoneClick(area);
      }}
    >
      {area}
    </div>
  );
})}
    </div>
  </div>
);

return (
  <>
    {/* Mobile Version */}
    {isMobile && (
      <>
        <WeatherDisplay className={styles.weatherSmall} />
        <div className={styles.sidebarContainer}>
          <div className={styles.dropdownTopMob}>
            <ActivityDropdown />
            <div className={styles.readableTimeContainer}>
              <DateDropdown />
              <TimeDropdown />
            </div>
            <button className={styles.subButtonMob} onClick={handleSubmit}>
              <FaCheck />
            </button>
            <button className={styles.buttonStyle} onClick={handleSubmit}>Submit</button>
          </div>
          
          <div className={styles.locationBottomSection}>
            {/* Mobile-only toggle button - moved inside mobile conditional */}
            <div className={styles.expandToggleContainer}>
              <button onClick={handleLocationToggle} className={styles.expandToggleBtn}>
                {showLocationContent ? (
                  <svg width="20" height="13" viewBox="0 0 25 13" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path opacity="0.9" d="M22.5 10.1353L12.3261 2.9999L3 10.1353" stroke="#177371" strokeWidth="4.5" strokeLinecap="round"/>
                  </svg>
                ) : (
                  <svg width="20" height="13" viewBox="0 0 25 13" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path opacity="0.9" d="M3 2.93237L13.1739 10.0677L22.5 2.93237" stroke="#177371" strokeWidth="4.5" strokeLinecap="round"/>
                  </svg>
                )}
              </button>
            </div>
            
            <div className={styles.locationHeader}>
              <div className={`${styles.recArea} ${showLocations ? styles.inactive : styles.active}`}>
                <p className={styles.recommendation}>Recommended</p>
                <p className={styles.area}>Area</p>
              </div>
              
              <button onClick={handleToggleClick} className={styles.areaToggleBtn}>
                {!showLocations ? (
                  <svg width="40" height="25" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M32 10H16C8.26801 10 2 16.268 2 24C2 31.732 8.26801 38 16 38H32C39.732 38 46 31.732 46 24C46 16.268 39.732 10 32 10Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
                    <path d="M32 29.999C35.3137 29.999 38 27.3127 38 23.999C38 20.6853 35.3137 17.999 32 17.999C28.6863 17.999 26 20.6853 26 23.999C26 27.3127 28.6863 29.999 32 29.999Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                ) : (
                  <svg width="40" height="25" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M32 10H16C8.26801 10 2 16.268 2 24C2 31.732 8.26801 38 16 38H32C39.732 38 46 31.732 46 24C46 16.268 39.732 10 32 10Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
                    <path d="M16 30C19.3137 30 22 27.3137 22 24C22 20.6863 19.3137 18 16 18C12.6863 18 10 20.6863 10 24C10 27.3137 12.6863 30 16 30Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" fill="#52767E" strokeLinejoin="round"/>
                  </svg>
                )}
              </button> 
              <span className={`${styles.manualSelection} ${showLocations ? styles.active : styles.inactive}`}>
                Select Area
              </span>
            </div>
            
            {/* Mobile location content - controlled by showLocationContent state */}
            {showLocationContent && (
              <>
                {!submitted && showLocations && (
                  <div className={styles.noRecommendations}>
                    Please submit your choices to view the recommended areas
                  </div>
                )}
                
                {submitted && showLocations && <LocationsList />}
                {!showLocations && <ZoneSelection />}
              </>
            )}
          </div>
        </div>
      </>
    )}

    {/* Desktop Version */}
    {isDesktop && (
      <div className={styles.sidebarContainer}>
        <div className={styles.dropdownTopMob}>
          <ActivityDropdown />
          <div className={styles.readableTimeContainer}>
            <DateDropdown />
            <TimeDropdown />
          </div>
          <button className={styles.subButtonMob} onClick={handleSubmit}>
            <FaCheck />
          </button>
          <button className={styles.buttonStyle} onClick={handleSubmit}>Submit</button>
          
          {isLarge && <WeatherDisplay />}
        </div>
        
        <div className={styles.locationBottomSection}>
          {/* Desktop location header - no expand toggle button */}
          <div className={styles.locationHeader}>
            <div className={`${styles.recArea} ${showLocations ? styles.active : styles.inactive}`}>
              <p className={styles.recommendation}>Recommended</p>
              <p className={styles.area}>Area</p>
            </div>
            
            <button onClick={handleToggleClick} className={styles.areaToggleBtn}>
              {!showLocations ? (
                <svg width="40" height="25" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M32 10H16C8.26801 10 2 16.268 2 24C2 31.732 8.26801 38 16 38H32C39.732 38 46 31.732 46 24C46 16.268 39.732 10 32 10Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M32 29.999C35.3137 29.999 38 27.3127 38 23.999C38 20.6853 35.3137 17.999 32 17.999C28.6863 17.999 26 20.6853 26 23.999C26 27.3127 28.6863 29.999 32 29.999Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              ) : (
                <svg width="40" height="25" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M32 10H16C8.26801 10 2 16.268 2 24C2 31.732 8.26801 38 16 38H32C39.732 38 46 31.732 46 24C46 16.268 39.732 10 32 10Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M16 30C19.3137 30 22 27.3137 22 24C22 20.6863 19.3137 18 16 18C12.6863 18 10 20.6863 10 24C10 27.3137 12.6863 30 16 30Z" stroke="#52767E" strokeWidth="4" strokeLinecap="round" fill="#52767E" strokeLinejoin="round"/>
                </svg>
              )}
            </button> 
            <span className={`${styles.manualSelection} ${showLocations ? styles.inactive : styles.active}`}>
              Select Area
            </span>
          </div>
          
          {/* Desktop location content - always visible (showLocationContent always true for desktop) */}
          {!submitted && showLocations && (
            <div className={styles.noRecommendations}>
              Please submit your choices to view the recommended areas
            </div>
          )}
          
          {submitted && showLocations && <LocationsList />}
          {!showLocations && <ZoneSelection />}
        </div>
      </div>
    )}
  </>
);
}