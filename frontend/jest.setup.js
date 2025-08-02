import '@testing-library/jest-dom';

const { TextDecoder, TextEncoder } = require('util');
global.TextDecoder = TextDecoder;
global.TextEncoder = TextEncoder;

const origError = console.error;
console.error = (...args) => {
  if (
    typeof args[0] === 'string' &&
    args[0].includes('Not implemented: navigation')
  ) {
    return;
  }
  if (
    args[0] instanceof Error &&
    typeof args[0].message === 'string' &&
    args[0].message.includes('Not implemented: navigation')
  ) {
    return;
  }
  origError(...args);
};

jest.mock('next/dynamic', () => (importFn) => {
  const mod = importFn();
  return mod.default || mod;
});

import { createMockRouter } from './mocks/nextRouterMock';    
import { useRouter } from 'next/router';

jest.mock('next/router', () => ({ useRouter: jest.fn() }));

jest.mock('next/navigation', () => ({
  useRouter: () => createMockRouter(),
  usePathname: () => '/', 
}));

jest.mock('next/link', () => {
  const Link = ({ href, children, ...props }) => (
    <a href={href} {...props}>{children}</a>
  );
  return { __esModule: true, default: Link, Link };
});

