'use client';
import { useEffect, useRef, useState } from "react";
import ActivityLetters from '@/helper/activity';
import DateLetters from "@/helper/date";
import TimeLetters from '@/helper/time';
import styles from '../styles/sidebar.module.css';
import '../globals.css';

export default function SideBar(){
    const [selected, setSelected] = useState('2');
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
                    
                        <input placeholder="Select..."></input>
                    
               
                </div>
                <div
                className={`${styles.sidebarInner} ${styles.time}`}
                >
                     <hr/>
                    <div className={`${styles.innerPosition}`}>
                    <div>
                    <p>Choose Your</p>
                    <div className={`${styles.dateContainer}`}>
                        <p><DateLetters/></p>
                        
                         <p><TimeLetters/></p>

                    </div>
                   
                    </div>
                 
                    </div>
                    <hr />

                </div>
          </div>
    </>
    );

}