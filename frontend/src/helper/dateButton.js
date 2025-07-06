
import {FaChevronDown,FaChevronUp} from 'react-icons/fa';
import './styles/dateButton.css';
const DateDropdown=({children,open,toggle})=>{
    return(
        <div onClick={toggle} className={`date-dropdown-btn ${open ? "date-button-open" :null}`}>{children} <span className='date-toggle-icon'>{open ?<FaChevronUp/>:<FaChevronDown/>}</span></div>
    );
}

export default DateDropdown;

