
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
                         <nav className='navigation-container-map '>
                            <ul className='navigation-list-map flex'>
                            <li><Link className='navigation-item-map' href="/">Home</Link></li>
                            <li><Link className='navigation-item-map' href="/map">Map</Link></li>
                            <li><Link className='navigation-item-map' href="/about">About</Link></li>
                            </ul>
                        </nav>
                        </>
                        
                        
                    ) : (
                        <>
                        <div className='logo-div-desktop'>
                            <Image className='logo' src='/manhattan-muse-home.png' alt="Home Logo" width={370} height={150}/>
                        </div>
                        <nav className='navigation-item-container '>
                            <ul className='navigation-list flex'>
                            <li><Link className='navigation-item' href="/">Home</Link></li>
                            <li><Link className='navigation-item' href="/map">Map</Link></li>
                            <li><Link className='navigation-item' href="/about">About</Link></li>
                            </ul>
                        </nav>

                        </>
                        
                    )}

                        <div className='logo-div-mobile'>
                            <Image className='logoMobile'src='/logo-mobile.png' alt="Manhattan Muse logo"   width={50}
                    height={70} quality={100}/>
                        </div>
      
                        
                        <div className='menuIcon'>
                            <Image src='/MenuButton.png' alt="Menu Button" width={24}
                    height={24} />
                        </div>

                </div>
            </div>
        </>
    );
}

