import {AiOutlineClose} from 'react-icons/ai';
import Dropdown from '../../dropdowns/actDropdown';
import DropdownItem from '@/helper/activityItem';
import styles from '../sidebar.module.css';


export default function ActivitySelector({
    activityChoice,
    activities,
    onSelect,
    onClear,
    className='',
}){
    return(
          <div className={`${styles.activityWrapper} ${className}`}>
    <Dropdown 
     
      buttonText={
        <span className={`${styles.buttonTextWrapper} ${activityChoice ? styles.selectedItem : ''}`}>
          {activityChoice?.name || 'Select Activity'}
          {activityChoice && (
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
      activityChoice={activityChoice}
      content={(close)=>(   
        <>
          
          {activities.map(activity => (
  <DropdownItem
    key={activity.id}
    onClick={() => {
        onSelect(activity);
      close();
    }}
    className={activityChoice?.id === activity.id ? styles.selectedItem : ''}
  >
    {activity.name}
  </DropdownItem>
))}
        </>
      )}
    />
  </div>
    );
}