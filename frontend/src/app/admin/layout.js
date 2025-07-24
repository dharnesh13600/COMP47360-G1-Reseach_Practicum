export const metadata = {
  title: 'Manhattan Muse',
  icons: {
    icon: [
      {
        url: '/favicon.ico',
        sizes: 'any',
      }
    ],
    shortcut: '/favicon.ico',
  },
};

export default function AdminLayout({ children }) {
  return (
     <html lang="en">
      
      <body>
   <div className="admin-wrapper">
      {children} 
    </div>
</body>
    </html>
  );
}