import './styles/dateItem.css';
import styles from '@/app/styles/sidebar.module.css';
const DateItem=({children,onClick, className=''})=>{
    
    return (
        <div className={`date-dropdown-item ${className}`} onClick={onClick}>{children}</div>
    );
}

export default DateItem;