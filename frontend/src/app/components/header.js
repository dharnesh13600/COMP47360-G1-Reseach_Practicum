

// https://svg-path.com/
// https://medium.com/@bragg/cubic-bezier-curves-with-svg-paths-a326bb09616f
// https://codepen.io/pen
// https://www.nan.fyi/svg-paths/cubic-curves

'use client';

import '../globals.css';
import MapDraw from '../components/map-draw-1.js'
import MapDraw02 from '../components/map-draw-2.js'
import '../styles/map-draw-1.css'
import '../styles/map-draw-2.css'

import Button from '../components/button';
import Link from 'next/link';
import '../styles/header.css';
import Image from 'next/image';
import { usePathname } from 'next/navigation'
import { useEffect, useState } from 'react';

export default function Header(){
 
    const pathname = usePathname()

    const isMapPage = pathname === '/map';
    const wrapperClassName = isMapPage ? 'navigation-wrapper-map' : 'navigation-wrapper';
    const [menuOpen, setMenuOpen] = useState(false);

    return(
        <>
            <div className='hero'>
                <div className='header'>
                </div>
                <div className={wrapperClassName}>
                    {isMapPage ? (
                         <>
                        <div className='logo-div-map'>
                             <MapDraw />
                            <Image className='logoMap' src='/manhattan-muse-map.png' alt="Map Logo" width={360} height={120}/>
                            <MapDraw02 />
                        </div>
                       
                  

                        <nav className={`navigation-container-map ${menuOpen ? 'menu-open' : ''}`}>
                            <ul className='navigation-list-map flex'>    
                            <li className='nav-home'>
                                <svg className='nav-draw-home' viewBox="0 0 30 30" preserveAspectRatio="xMidYMid meet" width="100%" height="100%">
                                    <path d="M-2,-15 Q-3,15 12.9,10 T30,15" 
                                    fill="none" stroke="orange" strokeWidth="1" />
                                </svg>
                                <Link className='navigation-item' href="/">Home</Link>
                            </li>
                            <li className='nav-map'>
                                <svg className='nav-draw-map' viewBox="0 0 30 30" preserveAspectRatio="xMidYMid meet" width="100%" height="10%">
                                    <path d="M-1,40 Q5,5 35,15" 
                                    fill="none" stroke="orange" strokeWidth="1.2" />
                                </svg>
                                <Link className='navigation-item' href="/map">Map</Link></li>
                            <li className='nav-about'>
                                <svg className='nav-draw-about' viewBox="0 0 30 30" preserveAspectRatio="xMidYMid meet" width="100%" height="10%">
                                    <path d="M3,23 Q18,24 16,11.5 T26,0"
                                    fill="none" stroke="orange" strokeWidth="1.2" />
                                </svg>
                                <Link className='navigation-item' href="/about">About</Link></li>
                            </ul>
                        </nav>
                 
                        </>
                        
                    ) : (
                        <>
                        <div className='logo-div-desktop'>
                            <Image className='logo' src='/manhattan-muse-home.png' alt="Home Logo" width={370} height={150}/>
                        </div>
                        <nav className={`navigation-item-container ${menuOpen ? 'menu-open' : ''}`}>
                            <ul className='navigation-list flex'>    
                            <li className='nav-home'>
                                <svg className='nav-draw-home' viewBox="0 0 30 30" preserveAspectRatio="xMidYMid meet" width="100%" height="100%">
                                    <path d="M-2,-15 Q-3,15 12.9,10 T30,15" 
                                    fill="none" stroke="orange" strokeWidth="1" />
                                </svg>
                                <Link className='navigation-item' href="/">Home</Link>
                            </li>
                            <li className='nav-map'>
                                <svg className='nav-draw-map' viewBox="0 0 30 30" preserveAspectRatio="xMidYMid meet" width="100%" height="10%">
                                    <path d="M-1,40 Q5,5 35,15" 
                                    fill="none" stroke="orange" strokeWidth="1.2" />
                                </svg>
                                <Link className='navigation-item' href="/map">Map</Link></li>
                            <li className='nav-about'>
                                <svg className='nav-draw-about' viewBox="0 0 30 30" preserveAspectRatio="xMidYMid meet" width="100%" height="10%">
                                    <path d="M3,23 Q18,24 16,11.5 T26,0"
                                    fill="none" stroke="orange" strokeWidth="1.2" />
                                </svg>
                                <Link className='navigation-item' href="/about">About</Link></li>
                            </ul>
                        </nav>
                        </>
                        
                    )}

                        <div className='logo-div-mobile'>
                            <Image className='logoMobile'src='/logo-mobile.png' alt="Manhattan Muse logo"   width={50}
                    height={70} quality={100}/>
                        </div>
      
                        

                        <div className={`menuIcon ${menuOpen ? 'open' : ''}`} onClick={() => setMenuOpen(!menuOpen)}>
                                <svg width="30" height="30" viewBox="0 0 30 30">
                                    <rect className="line top" y="5" width="30" height="3.5" rx="2" ry="2" />
                                    <rect className="line middle" y="13" width="30" height="3.5" rx="2" ry="2"  />
                                    <rect className="line bottom" y="21" width="30" height="3.5" rx="2" ry="2"  />
                                </svg>
                        
                        </div>

                        {/* Navigation Menu */}
                        {menuOpen && (
                            <div className="menuOverlay">
                                <nav>
                                    <ul>
                                        <li className='nav-home'><Link href="/">Home</Link></li>
                                        <li className='nav-map'><Link href="/map">Map</Link></li> 
                                        <li className='nav-about'><Link href="/about">About</Link></li>
                                    </ul>
                                </nav>
                            </div>
                        )}
                    </div>
            </div>
        </>
    );
}

