import {format} from 'date-fns';

export function isToday(dateStr){
const today=new Date();
  const todayStr=format(today, 'MMMM d');
  return dateStr===todayStr;
}

