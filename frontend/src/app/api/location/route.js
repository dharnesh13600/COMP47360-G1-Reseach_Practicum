export async function POST(req){
    
        const jsonBody=await req.json();

        const {activity,dateTime,selectedZone }=jsonBody;

        if(!activity || !dateTime){
            return Response.json(
                {error:'Missing required fields'},
                {status:400}
            );
        }
      
        return Response.json(
            {
                activity,
                dateTime,
                selectedZone
            }
        );

}