import Header from '../components/header';
import Activity from '@/helper/activity';
import '../styles/dummy.css';
import '../globals.css';




export default function Dummy(){
    return(
          <>
           <div className="map-info-card flex">
                  <div className='map-info activity'>
                  <hr />
                  <div className='activity-container flex'>
                    <div className='activity-title-wrapper'>
                    <p className='activity-title-top'>Choose Your</p>
                    <p className='activity-title-bottom' >activity</p>
                      <ul id='activity-title-bottom '>
                        <li className='activity-letter'>A</li>
                        <li className='activity-letter'>C</li>
                        <li className='activity-letter'>T</li>
                        <li className='activity-letter'>I</li>
                        <li className='activity-letter'>V</li>
                        <li className='activity-letter'>I</li>
                        <li className='activity-letter'>T</li>
                        <li className='activity-letter'>Y</li>
                      </ul>
                    </div>
                      
                      <a>
                      </a>
                  </div>
                  </div>
                  <div className='map-info time'>
                  <hr></hr>
                  </div>
               </div>
    </>
    );
  
}