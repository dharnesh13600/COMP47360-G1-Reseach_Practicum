'use client';
import { useEffect, useRef, useState } from 'react';

import TimeDropdown from '@/helper/timeButton';
import DropdownContent from '@/helper/timeDropContent';
import TimeItem from '@/helper/timeItem';
import styles from '@/app/styles/timeDrop.module.css';


const DropdownTime=({buttonText,content,selectedTime})=>{
     const [Timeopen,setTimeOpen]=useState(false);
      const dropdownRef = useRef(null);
    const toggleTime=()=>{
        setTimeOpen(prev => !prev);
    }
     const close = () => {
    setTimeOpen(false);
  };
     useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setTimeOpen(false);
      }
    };
    
    document.addEventListener('mousedown', handleClickOutside);

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);


    return(
        <>
        <div  ref={dropdownRef}  className={styles.timedropdown}>
            <TimeDropdown toggle={toggleTime} open={Timeopen} selectedTime={selectedTime}>{buttonText}</TimeDropdown>
            <DropdownContent open={Timeopen}>{  typeof content === 'function'
              ? content(close)  
              : content}</DropdownContent>
        </div>
        </>
    );
}

export default DropdownTime;