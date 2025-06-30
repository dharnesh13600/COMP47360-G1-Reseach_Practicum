import '../styles/home-draw-1.css';
import '../globals.css';

export default function Draw01(){
    return(
        <>
        <div className='draw'>

            <svg height="400" width="450"></svg>
            <path id="lineAB" d="M 100 350 l 150 -300" stroke="red" strokeWidth="4"/>
        </div>
        </>
    );
}