import './styles/dateDropContent.css';
const DropdownContent=({children,open,top})=>{
    console.log("DropdownContent open =", open);

    return <div className={`date-dropdown-content ${open ? "date-content-open" : null}`}>{children}</div>
}
export default DropdownContent;