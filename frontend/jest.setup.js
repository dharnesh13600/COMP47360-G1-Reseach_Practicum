// import '@testing-library/jest-dom';

// // Polyfill TextDecoder/TextEncoder for mapbox-gl
// const { TextDecoder, TextEncoder } = require('util');
// global.TextDecoder = TextDecoder;
// global.TextEncoder = TextEncoder;

// /* ---- 0. stub next/dynamic so dynamic imports render immediately ---- */
// jest.mock('next/dynamic', () => (importFn) => {
//   const mod = importFn();
//   return mod.default || mod;
// });

// // --- stub next/link so it renders as a plain <a> ---
// jest.mock('next/link', () => {
//   const Link = ({ href, children, ...p }) => <a href={href} {...p}>{children}</a>
//   return { __esModule: true, default: Link, Link }
// })
// /* ---- 1. global router mock ---- */
// import { createMockRouter } from './mocks/nextRouterMock';    
// import { useRouter } from 'next/router';

// jest.mock('next/router', () => ({ useRouter: jest.fn() }));

// jest.mock('next/navigation', () => ({
//   // <App Router hooks>
//   useRouter: () => createMockRouter(),
//   usePathname: () => '/', 
// }));

// /* ---- 2. stub <Link> so jsdom won’t attempt real navigation ---- */
// jest.mock('next/link', () => {
//   const Link = ({ href, children, ...props }) => (
//     <a href={href} {...props}>{children}</a>
//   );
//   return { __esModule: true, default: Link, Link };
// });

// /* ---- 3. (optional) if you’re still on the old Pages Router, swap block above:
// jest.mock('next/router', () => ({
//   useRouter: () => createMockRouter(),
//   usePathname: () => '/',      // only if your pages-router code calls it
// }));
// */

//-------------------------------------------

import '@testing-library/jest-dom';

// Polyfill TextDecoder/TextEncoder for mapbox-gl
const { TextDecoder, TextEncoder } = require('util');
global.TextDecoder = TextDecoder;
global.TextEncoder = TextEncoder;

// —————————————————————————————————————————————————————————
// Suppress JSDOM “Not implemented: navigation” warnings
const origError = console.error;
console.error = (...args) => {
  // suppress string-based navigation errors
  if (
    typeof args[0] === 'string' &&
    args[0].includes('Not implemented: navigation')
  ) {
    return;
  }
  // suppress Error-object navigation errors
  if (
    args[0] instanceof Error &&
    typeof args[0].message === 'string' &&
    args[0].message.includes('Not implemented: navigation')
  ) {
    return;
  }
  origError(...args);
};

/* ---- 0. stub next/dynamic so dynamic imports render immediately ---- */
jest.mock('next/dynamic', () => (importFn) => {
  const mod = importFn();
  return mod.default || mod;
});

/* ---- 1. global router mock ---- */
import { createMockRouter } from './mocks/nextRouterMock';    
import { useRouter } from 'next/router';

jest.mock('next/router', () => ({ useRouter: jest.fn() }));

jest.mock('next/navigation', () => ({
  useRouter: () => createMockRouter(),
  usePathname: () => '/', 
}));

/* ---- 2. stub <Link> so jsdom won’t attempt real navigation ---- */
jest.mock('next/link', () => {
  const Link = ({ href, children, ...props }) => (
    <a href={href} {...props}>{children}</a>
  );
  return { __esModule: true, default: Link, Link };
});

/* ---- 3. (optional) if you’re still on the old Pages Router, swap block above:
jest.mock('next/router', () => ({
  useRouter: () => createMockRouter(),
  usePathname: () => '/',      // only if your pages-router code calls it
}));
*/
