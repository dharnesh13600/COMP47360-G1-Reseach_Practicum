import './styles/activityButton.css';
import {FaChevronDown,FaChevronUp} from 'react-icons/fa';
const ActivityDropdown=({children,open,toggle,activityChoice})=>{
    return(
        <div onClick={toggle} className={`dropdown-btn ${open ? "button-open" :null}`}>{children} {!activityChoice && <span className='toggle-icon'>{open ?<FaChevronUp/>:<FaChevronDown/>}</span>} </div>
    );
}

export default ActivityDropdown;