'use client';
import {useState,useEffect} from 'react';
import './styles/dateDropContent.css';


const DropdownContent=({children,open,top})=>{
    
const [animateOpen, setAnimateOpen] = useState(false);


useEffect(() => {
  if (open) {
    requestAnimationFrame(() => {
      setAnimateOpen(true);
    });
  } else {
    setAnimateOpen(false);
    

  }
}, [open]);

   
    return <div className={`date-dropdown-content ${animateOpen ? "date-content-open" : ""}`}>{children}</div>
}
export default DropdownContent;