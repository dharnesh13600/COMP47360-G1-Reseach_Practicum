// tests/About.test.js
import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'

// 1) Stub next/dynamic so it just returns the real module immediately:
jest.mock('next/dynamic', () => {
  return (importFn, options) => {
    // call the dynamic import immediately
    const mod = importFn()
    return mod.default || mod
  }
})

// 2) Stub next/link so we don’t get warnings about jsdom navigation
jest.mock('next/link', () => {
  const Link = ({ href, children, ...props }) => <a href={href} {...props}>{children}</a>
  return { __esModule: true, default: Link, Link }
})

// 3) Now import the real component
import About from '../src/app/components/about'

describe('<About /> “Go To Map” link', () => {
  it('renders the Go To Map link pointing at /map', () => {
    render(<About />)

    // find the link by its accessible name
    const link = screen.getByRole('link', { name: /go to map/i })
    expect(link).toBeInTheDocument()
    expect(link).toHaveAttribute('href', '/map')
  })
})
