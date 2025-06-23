import '../styles/about.css';
import '../styles/header.css';
import '../globals.css';
import Link from 'next/link';

const teams = [
  {
    name: "Front-End Team",
    members: ["Diviyya Shree Iyappan", "Phirada Kanjanangkulpunt"],
  },
  {
    name: "Data Team",
    members: ["Rahul Murali", "Ting Li (Jaxton)"],
  },
  {
    name: "Back-end Team",
    members: ["Mark Tully", "Dharnesh Vasudev"],
  },
];

export default function About(){
    return(
        <>
            <div className='hero'>
            <div className='header'>
            </div>
            <div className='navigation-wrapper flex'>
                <div className='logo-div-desktop'>
                    <img className='logo' src="manhattan-muse-home.png" alt="logo" />
                </div>
                <div className='logo-div-mobile'>
                    <img className='logoMobile'src='logo-mobile.png'></img>
                </div>
                <div className='navigation-item-container '>
                    <ul className='navigation-list flex'>
                        {/* 
                        <li><a className='navigation-item' href='#'>Home</a></li>
                        <li><a className='navigation-item' href='#'>Map</a></li>
                        <li><a className='navigation-item' href='#'>About</a></li>
                        */}
                        <li>
                            <Link className="navigation-item" href="#">Home</Link>
                        </li>
                        <li>
                            <Link className='navigation-item' href='#'>Map</Link>
                        </li>
                        <li>
                            <Link className='navigation-item' href='#'>About</Link>
                        </li>                  
                    </ul>
                </div>
                <div className='menuIcon'>
                    <img src='MenuButton.png'>
                    </img>
                </div>
            </div>
            </div>
            <div className='container'>
                <aside className='split-left'>
                    <div className='photo-name'>
                        {teams.map((team, index) => (
                            <div key={index}>
                            <p><b>{team.name}</b></p>
                            {team.members.map((member, i) => (
                                <p key={i}>{member}</p>
                            ))}
                            </div>
                        ))}
                    </div>
                    <div className='team-photo'>
                        <img src='about-pic-diviyya.png'></img>
                        <img src='about-pic-phirada.png'></img>
                        <img src='about-pic-rahul.png'></img>
                        <img src='about-pic-jaxton.png'></img>
                        <img src='about-pic-mark.png'></img>
                        <img src='about-pic-darnesh.png'></img>
                    </div>
                </aside>
                <aside className='split-right'>
                    <p className='about-heading'> Meet the Muse Makers</p>
                    <p className='about-text'> 
                        We’re a small team of artists, technologists, and coders—united by a love 
                        for NYC and the people who bring it to life. <br></br>
                        <br></br>
                        With Manhattan Muse, our mission is to empower artists, performers, and 
                        creators by helping them find the perfect places in New York City to express 
                        their craft—intuitively, creatively, and confidently. <br></br>
                        <br></br>
                        Using real-time data like crowd density, event history and weather, our 
                        interactive map helps you choose the right location at the right time. 
                        Just set your preferences, and we'll do the rest. <br></br>
                        <br></br>
                        Ready to create something amazing? Try the map or follow us for new 
                        inspiration and spot to pick! 
                    </p>
                    <div className='map-button'>
                        <Link href="/map">
                        <button className='button-style'>Go To Map</button>
                        </Link>
                    </div>
                </aside>
            </div>
        </>
    );
}
