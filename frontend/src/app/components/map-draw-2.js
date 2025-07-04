
export default function MapDraw02(){
    return(
        <>
            <div className='draw-map02'>
                <svg height="100" width="400">
                    <path d='M15,75
                    Q85,80 95,0
                    ' 
                    fill='none' stroke='#F69B86' strokeWidth='2.75'/>

                    <g stroke="#F69B86" strokeWidth='3' fill="#F69B86">
                        <circle cx="15" cy="75" r="4.5" />
                    </g>
                </svg>
            </div>
        </>
    );
}