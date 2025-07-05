import './styles/activitydropcontent.css';
const DropdownContent=({children,open,top})=>{
    return <div className={`dropdown-content ${open ? "content-open" : null}`}>{children}</div>
}
export default DropdownContent;