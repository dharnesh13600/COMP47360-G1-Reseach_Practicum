import {AiOutlineClose} from 'react-icons/ai';
import DropdownTime from '../../dropdowns/timeDropdown';
import TimeItem from '@/helper/timeItem';
import styles from '../sidebar.module.css';

export default function TimeSelector({
    selectedTime,
    times,
    onSelect,
    onClear,
    className=''
}){
    return(
          <div className={`${styles.timeWrapper} ${className}`}>
    <DropdownTime 
      buttonText={
        <span className={`${styles.buttonTextWrapper} ${selectedTime ? styles.selectedItem : ''}`}>
          {selectedTime || "Time"}
          {selectedTime && (
            <AiOutlineClose
              size={16}
              onClick={(e)=>{
                e.stopPropagation();
                onClear();
              }}
              className={styles.clearIcon}
            />
          )}
        </span>
      } 
      selectedTime={selectedTime}
      content={(close)=>(
        <>
          {times.length === 0 && <div className={styles.dateEmptyText}>Select date</div>}
          {times.map(time=>(
            <TimeItem key={time} onClick={()=>{onSelect(time);close();}}>
              {time}
            </TimeItem>
          ))}
        </>
      )} 
    />
  </div>
    );
}