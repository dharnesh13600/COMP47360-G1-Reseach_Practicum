import Button from './components/button.js';
import Image from 'next/image';
export default function Page(){
    return (
        <>
               <div className='hero-body flex'>
                <div>
                    <Image className='hero-image' src='/statue_of_liberty.jpg' alt="description" width={400} height={400}/>
                </div>
                <div className='hero-content'>
                    <h2>Welcome to Manhattan Muse! <br></br>
Your creative guide to New York City.
</h2>
<br></br>
<p>Designed for artists, performers, photographers, and creators. Simply choose your activity and time&mdash; our interactive map helps you discover the best places in Manhattan to express your work. Whether you&rsquo;re looking for a high-traffic corner to busk, a quiet park to sketch, or the perfect light for portrait photography, Manhattan Canvas lets you plan with precision&mdash;using real-time data on crowds, weather, and more.</p>
<Button />
                </div>
            </div>
        </>
    );
}