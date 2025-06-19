// https://svg-path.com/
// https://medium.com/@bragg/cubic-bezier-curves-with-svg-paths-a326bb09616f
// https://codepen.io/pen
// https://www.nan.fyi/svg-paths/cubic-curves

// demo
import '../globals.css';
import  '../styles/header.css';
import Image from 'next/image';
export default function Header(){


    return(
        <>
            <div className='header'>
                <svg className='header-area'viewBox="0 0 340 90" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none">
                <path fill="#FFFBF6"  d="M -10 0 C 0 30 40 50 190 20 S 500 180 400 0 Z" />
            </svg>
            <div className='navigation-wrapper flex'>
                <div className='logo-div'>
                    <Image className='logo' src="/logo.png" alt="logo" width={100} height={100} />
                </div>
                <div className='navigation-item-container '>
                    <ul className='navigation-list flex'>
                        <li><a class='navigation-item' href='#'>Home</a></li>
                        <li><a class='navigation-item' href='#'>Map</a></li>
                        <li><a class='navigation-item' href='#'>About</a></li>
                    </ul>

                </div>
            </div>
            </div>
            
        </>
    );
}
