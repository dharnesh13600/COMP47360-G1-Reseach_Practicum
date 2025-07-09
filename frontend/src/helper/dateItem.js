import './styles/dateItem.css';
const DateItem=({children,onClick})=>{
    
    return (
        <div className='date-dropdown-item' onClick={onClick}>{children}</div>
    );
}

export default DateItem;