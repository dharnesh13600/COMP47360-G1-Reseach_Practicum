import {useMemo} from 'react';

const weatherIcons={
    clouds:'/clouds_day.png',
    clear:'/clear_day.png',
    rain:'/rain_day.png',
};

export function useWeather({condition,temperature}){
    return useMemo(()=>{
        const icon= weatherIcons[condition?.toLowerCase()] || '/atmosphere_day.png';

        console.log(temperature);
        return {icon,temp:temperature};
    },[condition,temperature]);
}