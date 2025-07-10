export async function POST(req){
    
        const jsonBody=await req.json();

        const {activity,readableTime }=jsonBody;

        if(!activity || !readableTime){
            return Response.json(
                {error:'Missing required fields'},
                {status:400}
            );
        }
        console.log(activity);
        console.log(readableTime);
        return Response.json(
            {
                activity,
                readableTime
            }
        );

}