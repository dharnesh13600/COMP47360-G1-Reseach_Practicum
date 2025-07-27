import Image from 'next/image';
import styles from '../sidebar.module.css';
import { cleanAndTruncate } from '../../utils/stringUtils';

export default function LocationsList({
    visibleLocations=[],
    visibleIndexes=[],
    maxItems=5,
    onLocationSelect,
    locations=[],
    isLarge=false,
    showAllLocations=false,
    setShowAllLocations,
}){
    return(
        <>
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
          <Image className={styles.lens} src='/search.png' alt='d' width={30} height={25}/>
        </span>
      </div>
    ))}
    
  </div>
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
        </>
    );
}