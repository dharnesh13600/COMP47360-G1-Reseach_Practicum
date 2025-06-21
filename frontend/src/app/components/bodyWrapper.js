'use client';
import { usePathname } from 'next/navigation';

export default function BodyWrapper({ children }) {
  const pathname = usePathname();
  const pageClass = pathname.startsWith('/map') ? 'map-page' : 'home-page';

  return <div className={`antialiased ${pageClass}`}>{children}</div>;
}
