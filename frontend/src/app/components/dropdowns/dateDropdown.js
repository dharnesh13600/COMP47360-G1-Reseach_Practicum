'use client';
import { useEffect, useRef, useState } from 'react';
import DateDropdown from '@/helper/dateButton';
import DropdownContent from '@/helper/dateDropContent';
import DropdownItem from '@/helper/dateItem';
import styles from './dateDrop.module.css';
const DropdownDate=({buttonText,content,selectedDate})=>{
     const [Dateopen,setDateOpen]=useState(false);
      const dropdownRef = useRef(null);
    const toggleDate=()=>{
    
        setDateOpen(prev => !prev);
    }
      const close = () => {
    setDateOpen(false);
  };
     useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDateOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);


    return(
        <>
        <div   ref={dropdownRef} className={styles.datedropdown}>
            <DateDropdown toggle={toggleDate} open={Dateopen} selectedDate={selectedDate}>{buttonText}</DateDropdown>
            <DropdownContent open={Dateopen}>{  typeof content === 'function'
              ? content(close)  
              : content}</DropdownContent>
        </div>
        </>
    );
}

export default DropdownDate;