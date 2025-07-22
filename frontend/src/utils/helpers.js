// Date and time utilities
export const getDayName = (dayNumber) => {
  const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  return days[dayNumber - 1] || 'Unknown';
};

export const formatDateTime = (dateTime) => {
  return new Date(dateTime).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const formatTime = (hour) => {
  return `${hour.toString().padStart(2, '0')}:00`;
};

// Number formatting utilities
export const formatNumber = (num) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M';
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K';
  }
  return num.toString();
};

// Status utilities
export const getStatusColor = (status) => {
  switch (status?.toUpperCase()) {
    case 'HEALTHY':
    case 'CONNECTED':
    case 'ACTIVE':
    case 'MONITORED':
      return 'text-green-600';
    case 'ERROR':
    case 'DISCONNECTED':
      return 'text-red-600';
    case 'WARNING':
      return 'text-yellow-600';
    default:
      return 'text-gray-600';
  }
};