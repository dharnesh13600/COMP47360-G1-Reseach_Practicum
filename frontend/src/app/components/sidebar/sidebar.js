'use client';
export const BACKEND_BASE = process.env.NEXT_PUBLIC_BACKEND_API_URL;
import {useEffect,useState} from "react";
import styles from './sidebar.module.css'
import Image from'next/image';
import {parse,format} from 'date-fns';
import { FaCheck } from 'react-icons/fa';

// importing hooks
import useActivities from "../hooks/useActivities";
import useWeatherData from "../hooks/useWeatherData";
import useDateTimes from "../hooks/useDateTimes";
import useRecommendations from "../hooks/useRecommendations";

// importing utility functions
import { sanitizeName,cleanAndTruncate } from "../utils/stringUtils";
import { isToday } from "../utils/dateUtils";

import { fetchZones } from "../utils/apiHelpers";
import { useWeather } from "./useWeather";


import ActivitySelector from "./dropdowns/ActivitySelector";
import DateSelector from "./dropdowns/DateSelector";
import TimeSelector from "./dropdowns/TimeSelector";
import WeatherDisplay from "./dropdowns/WeatherDisplay";
import LocationsList from "./dropdowns/LocationsList";
import ZoneSelector from "./ZoneSelector";
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
const [selectedDate, setSelectedDate] = useState(null);
const [selectedTime, setSelectedTime] = useState(null);
const [submitted, setSubmitted] = useState(false);
const [hasSubmittedOnce, setHasSubmittedOnce] = useState(false);
const {activities,loading:activitiesLoading}=useActivities();
const {weather,loading:weatherLoading}=useWeatherData(selectedDate,selectedTime,submitted);
const {availableDates:dates,availableTimes:times,loading:datesLoading,error:datesError,}=useDateTimes(selectedDate);
const [manhattanNeighborhoods, setManhattan]=useState([]);
const [activityChoice, setChoice]=useState(null);
const [visibleIndexes, setVisibleIndexes] = useState([]);
const [zone, setZone]=useState(null);
const [zonesLoaded, setZonesLoaded] = useState(false);


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

const [showLocationContent, setShowLocationContent] = useState(false);

const handleToggleClick = () => {
  setClearMarkers(true);
  setShowLocations((prev) => !prev);
};

const handleLocationToggle = () => {
  setShowLocationContent((prev) => !prev);
};

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

useEffect(() => {
  onSelectedTimeChange(selectedTime);
}, [selectedTime, onSelectedTimeChange]);

const {icon,temp}=useWeather(weather || {});

const {handleSubmit,handleZoneClick}=useRecommendations(

  onSubmit,
  onZoneResults

);
useEffect(() => {
  
  if (submitted) {
    setVisibleIndexes([]);
    visibleLocations.forEach((_, i) => {
      let delay=100 +i*180;
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


useEffect(() => {
  if (isDesktop) {
    setShowLocationContent(true); 
  } else {
    setShowLocationContent(false); 
  }
}, [isDesktop]);

const maxItems = showAllLocations ? visibleLocations.length : 5;
return (
  <>
    {/* Mobile Version */}
    {isMobile && (
      <>
        <WeatherDisplay weather={weather} icon={icon} temp={temp} submitted={submitted} className={styles.weatherSmall}/>
        <div className={styles.sidebarContainer}>
          <div className={styles.dropdownTopMob}>
           
            <ActivitySelector  activityChoice={activityChoice} activities={activities} onSelect={(a)=>{setChoice(a);setSubmitted(false);}}  onClear={()=>{setChoice(null); setSubmitted(false);}}/>
            <div className={styles.readableTimeContainer}>
              <DateSelector  selectedDate={selectedDate} dates={dates} onSelect={(d)=>{setSelectedDate(d);setSubmitted(false);}} onClear={()=>{setSelectedDate(null); setSubmitted(false);}}/>
              <TimeSelector selectedTime={selectedTime} times={times} onSelect={(t)=>{setSelectedTime(t);setSubmitted(false);}} onClear={()=>{setSelectedTime(null);setSubmitted(false);}}/>
            </div>
            <button className={styles.subButtonMob} onClick={() => handleSubmit({ selectedDate, selectedTime, activityChoice,setSubmitted,
    setHasSubmittedOnce })} disabled={isDesktop && !showLocations} style={{ opacity:isDesktop && !showLocations ? 0.5 : 1, cursor: isDesktop && !showLocations ? 'not-allowed' : 'pointer' }}>
              <FaCheck />
            </button>
            <button className={styles.buttonStyle} onClick={() => handleSubmit({ selectedDate, selectedTime, activityChoice,setSubmitted,
    setHasSubmittedOnce })} disabled={isDesktop && !showLocations} style={{ opacity: isDesktop && !showLocations ? 0.5 : 1, cursor: isDesktop && !showLocations ? 'not-allowed' : 'pointer' }}>Submit</button>
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
                    <path opacity="0.9" d="M22.5 10.1353L12.3261 2.9999L3 10.1353" stroke="#177371" strokeWidth="4.5" strokeLinecap="round"/>
                  </svg>
                )}
              </button>
            </div>
           {hasSubmittedOnce && 
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
          }
             {!submitted && showLocations && (
                  <div className={styles.noRecommendations}>
                    Please submit your choices to view the recommended areas
                  </div>
                )}
            
            {showLocationContent && (
              <>
               
                
                {submitted && showLocations && <LocationsList visibleLocations={visibleLocations} visibleIndexes={visibleIndexes} maxItems={maxItems} onLocationSelect={onLocationSelect} locations={locations} isLarge={isLarge} showAllLocations={showAllLocations} setShowAllLocations={setShowAllLocations}/>}
                {!showLocations && <ZoneSelector
  manhattanNeighborhoods={manhattanNeighborhoods}
  selectedDate={selectedDate}
  selectedTime={selectedTime}
  activityChoice={activityChoice}
  handleZoneClick={handleZoneClick}
  setZone={setZone}
  isSmall={isSmall}
  showAllLocations={showAllLocations}
/>}
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
            <ActivitySelector  activityChoice={activityChoice} activities={activities} onSelect={(a)=>{setChoice(a);setSubmitted(false);}}  onClear={()=>{setChoice(null); setSubmitted(false);}}/>
          <div className={styles.readableTimeContainer}>
             <DateSelector  selectedDate={selectedDate} dates={dates} onSelect={(d)=>{setSelectedDate(d);setSubmitted(false);}} onClear={()=>{setSelectedDate(null); setSubmitted(false);}}/>
            <TimeSelector selectedTime={selectedTime} times={times} onSelect={(t)=>{setSelectedTime(t);setSubmitted(false);}} onClear={()=>{setSelectedTime(null);setSubmitted(false);}}/>
          </div>
          
          <button className={styles.buttonStyle} onClick={() => handleSubmit({ selectedDate, selectedTime, activityChoice,setSubmitted,
    setHasSubmittedOnce })} disabled={!showLocations} style={{ opacity: !showLocations ? 0.5 : 1, cursor: !showLocations ? 'not-allowed' : 'pointer' }}>Submit</button>
          
          {isLarge && <WeatherDisplay weather={weather} icon={icon} temp={temp} submitted={submitted}/>}
        </div>
        
        <div className={styles.locationBottomSection}>
       
 
          
          {hasSubmittedOnce && 
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
          }
          
          
          {!submitted && showLocations && (
            <div className={styles.noRecommendations}>
              Please submit your choices to view the recommended areas
            </div>
          )}
          
          {submitted && showLocations &&  <LocationsList visibleLocations={visibleLocations} visibleIndexes={visibleIndexes} maxItems={maxItems} onLocationSelect={onLocationSelect} locations={locations} isLarge={isLarge} showAllLocations={showAllLocations} setShowAllLocations={setShowAllLocations}/>}
          {!showLocations && <ZoneSelector
  manhattanNeighborhoods={manhattanNeighborhoods}
  selectedDate={selectedDate}
  selectedTime={selectedTime}
  activityChoice={activityChoice}
  handleZoneClick={handleZoneClick}
  setZone={setZone}
  isSmall={isSmall}
  showAllLocations={showAllLocations}
/>}
        </div>
      </div>
    )}
  </>
);
}
