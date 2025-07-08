import {useMemo} from 'react';

const weatherIcons={
    clouds:'/clouds_day.png',
    clear:'/clear_day.png',
    rain:'/rain_day.png',
};

export function useWeather({condition,temp}){
    return useMemo(()=>{
        const icon= weatherIcons[condition?.toLowerCase()] || '/atmosphere_day.png';

        const roundedTemp= typeof temp === 'number' ? Math.round(temp):null;

        return {icon,temp:roundedTemp};
    },[condition,temp]);
}