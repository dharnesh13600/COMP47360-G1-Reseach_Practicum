export default function Activity(){
var colors = ['#288FE4', '#FFE87E', '#DB7477', '#1BAA5E', '#DB7477', '#DB7477', '#288FE4', '#C78750'];
var listItems = document.getElementById('paragraph').getElementsByTagName('li');

for (var i = 0; i < listItems.length; i++) {
 listItems[i].style.backgroundColor = colors[i % colors.length];
 listItems[i].style.width = '27px';
     listItems[i].style.height = '27px';
  
  listItems[i].style.borderRadius = '50%';
 listItems[i].style.color='#fff';
  listItems[i].style.display = 'inline-flex';
  listItems[i].style.alignItems = 'center';
  listItems[i].style.justifyContent = 'center';
  listItems[i].style.margin = '1.5px';
  listItems[i].style.fontWeight = 'bold';
  listItems[i].style.fontFamily = 'Inter';
  listItems[i].style.fontSize='20px';
 
}



}
