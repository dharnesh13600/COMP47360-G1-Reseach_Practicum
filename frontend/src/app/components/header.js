// https://svg-path.com/
// https://medium.com/@bragg/cubic-bezier-curves-with-svg-paths-a326bb09616f
// https://codepen.io/pen
// https://www.nan.fyi/svg-paths/cubic-curves


import '../globals.css';

import Button from '../components/button';
import Link from 'next/link';
import '../styles/header.css';


export default function Header(){
 


    return(
        <>
            <div className='hero'>
            <div className='header'>
            </div>
            <div className='navigation-wrapper'>
                          <div className='logo-div-map'>
                    <img className='logoMap' src='manhattan-muse-map.png' alt="description"></img>
                </div>
                <div className='logo-div-desktop'>
                    <img className='logo' src="manhattan-muse-home.png" alt="logo" />
                </div>
                <div className='logo-div-mobile'>
                    <img className='logoMobile'src='logo-mobile.png' alt="description"></img>
                </div>
      
                <div className='navigation-item-container '>
                    
                    <ul className='navigation-list flex'>
  <li><Link className='navigation-item' href="/">Home</Link></li>
  <li><Link className='navigation-item' href="/map">Map</Link></li>
  <li><Link className='navigation-item' href="/about">About</Link></li>
</ul>

                </div>
                <div className='menuIcon'>
                    <img src='MenuButton.png' alt="description">
                    </img>
                </div>
            </div>
         
            </div>
        </>
    );
}

