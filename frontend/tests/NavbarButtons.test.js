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

jest.mock('next/navigation', () => ({
  useRouter: () => createMockRouter(),
  usePathname: () => '/',
}));

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
    jest
      .spyOn(require('next/navigation'), 'usePathname')
      .mockReturnValueOnce('/map');

    const { container } = render(<Navbar />);

    const wrapper = container.querySelector('.navigation-wrapper-map');
    expect(wrapper).toBeInTheDocument();               
    expect(wrapper).toHaveClass('navigation-wrapper-map');
  });

  it('“MAP” link points to /map and is clickable', async () => {
    const user = userEvent.setup();
    render(<Navbar />);

    const mapLink = screen.getByRole('link', { name: /map/i });
    expect(mapLink).toHaveAttribute('href', '/map');
    await user.click(mapLink);
  });
});