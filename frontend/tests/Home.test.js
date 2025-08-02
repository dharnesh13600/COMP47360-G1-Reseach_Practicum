import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

const mockPush = jest.fn()
jest.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}))

import Home from '../src/app/(site)/page'  

describe('<Home /> “Go To Map” button', () => {
  beforeEach(() => {
    mockPush.mockClear()
  })

  it('renders the Go To Map button and navigates to /map on click', async () => {
    render(<Home />)

   const link = screen.getByRole('link', { name: /go to map/i })
   expect(link).toBeInTheDocument()
  
   expect(link).toHaveAttribute('href', '/map')
  })
})
