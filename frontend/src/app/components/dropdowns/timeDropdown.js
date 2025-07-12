'use client';
import { useEffect, useRef, useState } from 'react';

import TimeDropdown from '@/helper/timeButton';
import DropdownContent from '@/helper/timeDropContent';
import TimeItem from '@/helper/timeItem';
import styles from '@/app/styles/timeDrop.module.css';


const DropdownTime=({buttonText,content,selectedTime})=>{
     const [Timeopen,setDateOpen]=useState(false);
      const dropdownRef = useRef(null);
    const toggleTime=()=>{
        console.log("Dropdown toggle clicked");
        setDateOpen(prev => !prev);
    }
     useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDateOpen(false);
      }
    };
    const close =()=>setIsTimeOpen(false);
    document.addEventListener('mousedown', handleClickOutside);

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);
console.log("Rendering DateDropdown");

    return(
        <>
        <div  ref={dropdownRef}  className={styles.timedropdown}>
            <TimeDropdown toggle={toggleTime} open={Timeopen} selectedTime={selectedTime}>{buttonText}</TimeDropdown>
            <DropdownContent open={Timeopen}>{content}</DropdownContent>
        </div>
        </>
    );
}

export default DropdownTime;