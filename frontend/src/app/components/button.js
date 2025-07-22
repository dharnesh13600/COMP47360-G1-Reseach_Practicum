import '../styles/button.css';
import Link from 'next/link';

export default function Button(){
    return(
    <>
    <div>
        <Link className='button-style' href="/map">Go To Map</Link>
    </div>
    </>
    );
}