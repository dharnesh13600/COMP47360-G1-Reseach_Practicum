import './styles/timeDropContent.css';
const DropdownContent=({children,open,top})=>{
    

    return <div className={`time-dropdown-content ${open ? "time-content-open" : null}`}>{children}</div>
}
export default DropdownContent;