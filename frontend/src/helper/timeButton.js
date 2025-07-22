import {FaChevronDown,FaChevronUp} from 'react-icons/fa';
import './styles/timeButton.css';
const TimeDropdown=({children,open,toggle,selectedTime})=>{
    return(
        <div onClick={toggle} className={`time-dropdown-btn ${open ? "time-button-open" :null}`}>{children} {!selectedTime && <span className='time-toggle-icon'>{open ?<FaChevronUp/>:<FaChevronDown/>}</span> } </div>
    );
}

export default TimeDropdown;