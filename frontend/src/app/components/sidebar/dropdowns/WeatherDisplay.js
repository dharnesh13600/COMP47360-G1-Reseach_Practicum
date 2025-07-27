import Image from 'next/image';
import styles from '../sidebar.module.css';

export default function WeatherDisplay({
    weather,
    icon,
    temp,
    submitted,
    className=''
}){
    if(!submitted || !weather) return null;

    return(
         <div className={`${styles.weatherDisplay} ${className} ${submitted && weather ? styles.show : ''}`}>
   
        <Image
          src={icon}
          alt={weather.condition}
          width={32}
          height={32}
          style={{ marginRight: '8px' }}
        />
        <span>{temp}°F</span>
      
    
  </div>
    );
}