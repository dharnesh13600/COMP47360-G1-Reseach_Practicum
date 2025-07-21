// tests/About.test.js
import React from 'react'
import '@testing-library/jest-dom'
import { render, screen } from '@testing-library/react'

// ─── Stub next/link so it renders a plain <a> ────────────────────────────────
jest.mock('next/link', () => {
  const Link = ({ href, children, ...props }) => (
    <a href={href} {...props}>
      {children}
    </a>
  )
  return { __esModule: true, default: Link, Link }
})

// ─── Import your About page’s default export ────────────────────────────────
import About from '../src/app/about/page'

describe('<About /> “Go To Map” link', () => {
  it('renders the Go To Map link pointing at /map', () => {
    render(<About />)

    // find the link by its accessible name
    const link = screen.getByRole('link', { name: /go to map/i })
    expect(link).toBeInTheDocument()
    expect(link).toHaveAttribute('href', '/map')
  })
})
