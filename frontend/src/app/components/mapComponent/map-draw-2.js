
export default function MapDraw02(){
    return(
        <>
            <div className='draw-map02'>
                <svg height="100" width="100">
                    <path className="draw-map-2-line" d='M95,0
                    Q85,80 5,85
                    ' 
                    fill='none' stroke='#F69B86' strokeWidth='2.75'/>

                    <g stroke="#F69B86" strokeWidth='3' fill="#F69B86">
                        <circle className="map-draw-2-circle" cx="7" cy="85" r="4.5" />
                    </g>
                </svg>
            </div>
        </>
    );
}