import './styles/timeItem.css';
const TimeItem=({children,onClick})=>{
   
    return (
        <div className='time-dropdown-item' onClick={onClick}>{children}</div>
    );
}

export default TimeItem;