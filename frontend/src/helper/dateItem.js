import './styles/dateItem.css';
import styles from '@/app/styles/sidebar.module.css';
const DateItem=({children,onClick, className=''})=>{
    
    return (
        <div className={` ${className} date-dropdown-item`} onClick={onClick}>{children}</div>
    );
}

export default DateItem;