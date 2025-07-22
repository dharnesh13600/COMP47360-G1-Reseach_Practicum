import './styles/dateDropContent.css';
const DropdownContent=({children,open,top})=>{
   
    return <div className={`date-dropdown-content ${open ? "date-content-open" : null}`}>{children}</div>
}
export default DropdownContent;