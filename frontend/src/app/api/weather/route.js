// https://nextjs.org/docs/app/getting-started/route-handlers-and-middleware

export async function GET(){
    const baseUrl='http://localhost:3000';
    try{
         const response= await fetch(`${baseUrl}/forecast.json`,{
        cache:'no-store',
    });
    
    if(!response.ok){
        console.error(`Failed to fetch ${response.status}`);
        return Response.json(
            {error: 'Failed to fetch data'},
            {status:500}
        )
    }

    const data=await response.json();
    return Response.json({data});
    }
    catch(err){
        console.error(err);
    }



}
