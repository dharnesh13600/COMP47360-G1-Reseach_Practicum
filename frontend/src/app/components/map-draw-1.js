
export default function MapDraw(){
    return(
        <>
            <div className='draw-map01'>
                <svg height="200" width="400">
                    <path d='M120,10 
                    Q30,20 60,135
                    ' 
                    fill='none' stroke='#F69B86' strokeWidth='2.75'/>

                    <g stroke="#F69B86" strokeWidth='3' fill="#F69B86">
                        <circle cx="120" cy="10" r="4.5" />
                        <circle cx="60" cy="135" r="4.5" />
                    </g>
                </svg>
            </div>
        </>
    );
}