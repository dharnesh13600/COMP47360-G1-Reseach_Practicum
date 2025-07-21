/**
 * Tests the “HOME / MAP / ABOUT” links in the header.
 * Save as: frontend/tests/NavbarButtons.test.js
 */

/* ── 0.  Mocks MUST be defined BEFORE the component is imported ───────── */
jest.mock('next/link', () => {
  const Link = ({ href, children, ...props }) => (
    <a href={href} {...props}>{children}</a>
  );
  return { __esModule: true, default: Link, Link };
});

import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Navbar from '../src/app/components/header';
import { createMockRouter } from './nextRouterMock';

/* ── 1.  Next-router hooks ───────────────────────────────────────────── */
jest.mock('next/navigation', () => ({
  useRouter: () => createMockRouter(),
  usePathname: () => '/',            // default route
}));

/* ── 2.  Tests ───────────────────────────────────────────────────────── */
describe('<Navbar> buttons', () => {
  it('renders HOME · MAP · ABOUT with correct hrefs', () => {
    render(<Navbar />);

    expect(screen.getByRole('link', { name: /home/i }))
      .toHaveAttribute('href', '/');
    expect(screen.getByRole('link', { name: /map/i }))
      .toHaveAttribute('href', '/map');
    expect(screen.getByRole('link', { name: /about/i }))
      .toHaveAttribute('href', '/about');
  });

  it('adds wrapper modifier when pathname = /map', () => {
    // override the hook for THIS render
    jest
      .spyOn(require('next/navigation'), 'usePathname')
      .mockReturnValueOnce('/map');

    /* render with /map */
    const { container } = render(<Navbar />);

    // Query the wrapper by its class (no role attribute on the <div>)
    const wrapper = container.querySelector('.navigation-wrapper-map');
    expect(wrapper).toBeInTheDocument();               // sanity-check
    expect(wrapper).toHaveClass('navigation-wrapper-map');
  });

  it('“MAP” link points to /map and is clickable', async () => {
    const user = userEvent.setup();
    render(<Navbar />);

    const mapLink = screen.getByRole('link', { name: /map/i });
    expect(mapLink).toHaveAttribute('href', '/map');
    await user.click(mapLink);                         // proves it’s interactive
  });
});