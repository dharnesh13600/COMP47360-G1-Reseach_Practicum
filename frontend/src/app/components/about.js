import '../styles/about.css';
import '../globals.css';
import Link from 'next/link';
import Image from'next/image';
import Button from './button.js';
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
                        <Image className='photo' src='/about-pic-diviyya.png' alt='d' width={150} height={150}/>
                        <Image className='photo' src='/about-pic-phirada.png' alt='phi' width={150} height={150}/>
                        <Image className='photo' src='/about-pic-rahul.png' alt='ra' width={150} height={150}/>
                        <Image className='photo' src='/about-pic-jaxton.png' alt='j' width={150} height={150}/>
                        <Image className='photo' src='/about-pic-mark.png' alt='m' width={150} height={150}/>
                        <Image className='photo' src='/about-pic-darnesh.png' alt='dar' width={150} height={150}/>
                    </div>
                </aside>
                <aside className='split-right'>
                    <p className='about-heading'> Meet the Muse Makers</p>
                    <p className="about-text">
                      We&#39;re a small team of artists, technologists, and coders—united by a love 
                      for NYC and the people who bring it to life. <br />
                      <br />
                      With Manhattan Muse, our mission is to empower artists, performers, and 
                      creators by helping them find the perfect places in New York City to express 
                      their craft—intuitively, creatively, and confidently. <br />
                      <br />
                      Using real-time data like crowd density, event history and weather, our 
                      interactive map helps you choose the right location at the right time. 
                      Just set your preferences, and we&#39;ll do the rest. <br />
                      <br />
                      Ready to create something amazing? Try the map or follow us for new 
                      inspiration and spot to pick!
                    </p>
                    <div className='map-button'>
                      <Button/>
                    </div>
                </aside>
            </div>
        </>
    );
}
