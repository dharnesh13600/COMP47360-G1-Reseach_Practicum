export default function MapDraw(){
    return(
        <>
            <div className='draw-map01'>
                <svg height="150" width="120">
                    <path className="draw-map-1-line" d='M100,40 
                    Q40,50 50,130
                    ' 
                    fill='none' stroke='#F69B86' strokeWidth='2.75'/>

                    <g stroke="#F69B86" strokeWidth='3' fill="#F69B86">
                        <circle className="map-draw-1-circle-top" cx="100" cy="40" r="4.5" />
                        <circle className="map-draw-1-circle-bottom" cx="50" cy="130" r="4.5" />
                    </g>
                </svg>
            </div>
        </>
    );
}