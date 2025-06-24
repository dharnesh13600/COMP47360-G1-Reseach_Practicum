import '../styles/button.css';
import Link from 'next/link';

export default function Button(){
    return(
    <>
    <div className='map-button'>
        <Link className='button-style' href="/map">Go To Map</Link>
    </div>
    </>
    );
}