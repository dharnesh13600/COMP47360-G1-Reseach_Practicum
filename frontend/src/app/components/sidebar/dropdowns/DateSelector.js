import {AiOutlineClose} from 'react-icons/ai';
import DropdownDate from '../../dropdowns/dateDropdown';
import DateItem from '@/helper/dateItem';
import styles from '../sidebar.module.css';
import { isToday } from '../../utils/dateUtils';

export default function DateSelector({
    selectedDate,
    dates,
    onSelect,
    onClear,
    className=''
}){
    return(
        <div className={`${styles.dateWrapper} ${className}`}>
    <DropdownDate 
      buttonText={
        <span className={`${styles.buttonTextWrapper} ${selectedDate ? styles.selectedItem : ''} ${styles.otherContent}`}>
          {selectedDate ? (isToday(selectedDate) ? 'Today' : selectedDate) : 'Date'}
          {selectedDate && (
            <AiOutlineClose
              size={16}
              onClick={(e)=>{
                e.stopPropagation();
                onClear();
              }}
              className={`${styles.clearIcon}`}
            />
          )}
        </span>
      }
      selectedDate={selectedDate}
      content={(close)=>(
        <>
          {dates.map(date=>(
            <DateItem 
              key={date} 
              onClick={()=>{onSelect(date);requestAnimationFrame(close); }} 
              className={date === selectedDate ? styles.selectedItem : ''}
            >
              {isToday(date) ? 'Today' : date}
            </DateItem>
          ))}
        </>
      )} 
    />
  </div>
    );
}