import './styles/timeItem.css';
const TimeItem=({children,onClick})=>{
    console.log("Items");
    return (
        <div className='time-dropdown-item' onClick={onClick}>{children}</div>
    );
}

export default TimeItem;