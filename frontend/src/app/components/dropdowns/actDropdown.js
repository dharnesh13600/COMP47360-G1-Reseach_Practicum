'use client';
import { useEffect, useRef, useState } from 'react';
import ActivityDropdown from "@/helper/ActivityButton";
import DropdownContent from "@/helper/ActivityDropContent";
import DropdownItem from "@/helper/activityItem";
import styles from './dropdown.module.css';


const Dropdown=({buttonText,content,activityChoice})=>{
    const [open,setOpen]=useState(false);
     const dropdownRef = useRef(null);
    const toggleDropdown=()=>{
        setOpen((open) =>!open);
    }
      useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);
    return(
        <>
        <div  ref={dropdownRef} className={styles.dropdown}>
            <ActivityDropdown toggle={toggleDropdown} open={open} activityChoice={activityChoice}>{buttonText}</ActivityDropdown>
            <DropdownContent open={open}>{typeof content==='function'?content(() => setOpen(false)):content}</DropdownContent>
        </div>
        </>
    );
}

export default Dropdown;