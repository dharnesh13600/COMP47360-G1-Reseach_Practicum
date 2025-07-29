import styles from './sidebar.module.css';

export default function ZoneSelector({
    manhattanNeighborhoods=[],
    selectedDate,
    selectedTime,
  activityChoice,
  handleZoneClick,
  setZone,
  isSmall = false,
  showAllLocations = false,
}){
    return(
          <div className={styles.suggestedLocations}>
    <span>Select Area</span>
    <div className={`${styles.suggestedItems}${isSmall && showAllLocations ? styles.compact : ''}`}>
    
     {manhattanNeighborhoods.map(area => {
  console.log(typeof area, area); 

  return (
    <div
      key={area}
      className={styles.suggestedAreas}
      onClick={() => {
        handleZoneClick({
    selectedDate,
    selectedTime,
    activityChoice,
    area,
    setZone,
  })
      }}
    >
      {area}
    </div>
  );
})}
    </div>
  </div>
    );
}