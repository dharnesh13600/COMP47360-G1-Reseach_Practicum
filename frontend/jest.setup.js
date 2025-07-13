import '@testing-library/jest-dom';   // adds .toBeInTheDocument(), .toHaveClass(), etc.

/* ---- 1. global router mock ---- */
import { createMockRouter } from './mocks/nextRouterMock';   // <── two underscores!

jest.mock('next/navigation', () => ({
  // <App Router hooks>
  useRouter: () => createMockRouter(),
  usePathname: () => '/',          // default pathname seen by most tests
}));

/* ---- 2. stub <Link> so jsdom won’t attempt real navigation ---- */
jest.mock('next/link', () => {
  // Next 13–15 export both `default` *and* a named `Link`
  const Link = ({ href, children, ...props }) => (
    <a href={href} {...props}>
      {children}
    </a>
  );
  return { __esModule: true, default: Link, Link };
});

/* ---- 3. (optional) if you’re still on the old Pages Router, swap block above:
jest.mock('next/router', () => ({
  useRouter: () => createMockRouter(),
  usePathname: () => '/',      // only if your pages-router code calls it
}));
*/
