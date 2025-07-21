// tests/Home.test.js
import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

// 1) Mock whatever router hook your component uses.
//    If you’re on App‑Router, it’s next/navigation’s useRouter:
const mockPush = jest.fn()
jest.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}))

// 2) Import your Home page component
import Home from '../src/app/page'  // adjust to your path

describe('<Home /> “Go To Map” button', () => {
  beforeEach(() => {
    mockPush.mockClear()
  })

  it('renders the Go To Map button and navigates to /map on click', async () => {
    render(<Home />)

    // 5) Assert router.push was called with the correct path
    // now we look for a link (role="link"), not a button
   const link = screen.getByRole('link', { name: /go to map/i })
   expect(link).toBeInTheDocument()
   // it should point at "/map"
   expect(link).toHaveAttribute('href', '/map')
  })
})
