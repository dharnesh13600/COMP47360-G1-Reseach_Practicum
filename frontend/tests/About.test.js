import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'

jest.mock('next/dynamic', () => {
  return (importFn, options) => {
    const mod = importFn()
    return mod.default || mod
  }
})

jest.mock('next/link', () => {
  const Link = ({ href, children, ...props }) => <a href={href} {...props}>{children}</a>
  return { __esModule: true, default: Link, Link }
})

import About from '../src/app/components/about'

describe('<About /> “Go To Map” link', () => {
  it('renders the Go To Map link pointing at /map', () => {
    render(<About />)

    const link = screen.getByRole('link', { name: /go to map/i })
    expect(link).toBeInTheDocument()
    expect(link).toHaveAttribute('href', '/map')
  })
})
