'use client';
import { useEffect, useRef, useState } from 'react';
import DateDropdown from '@/helper/dateButton';
import DropdownContent from '@/helper/dateDropContent';
import DropdownItem from '@/helper/dateItem';
import styles from '@/app/styles/dateDrop.module.css';
const DropdownDate=({buttonText,content})=>{
     const [Dateopen,setDateOpen]=useState(false);
      const dropdownRef = useRef(null);
    const toggleDate=()=>{
        console.log("Dropdown toggle clicked");
        setDateOpen(prev => !prev);
    }
     useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDateOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);
console.log("Rendering DateDropdown");

    return(
        <>
        <div   ref={dropdownRef} className={styles.datedropdown}>
            <DateDropdown toggle={toggleDate} open={Dateopen}>{buttonText}</DateDropdown>
            <DropdownContent open={Dateopen}>{content}</DropdownContent>
        </div>
        </>
    );
}

export default DropdownDate;