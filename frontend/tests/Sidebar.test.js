import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
// import Sidebar from '../src/app/components/sidebar/sidebar'
// ⬅️ ADDED: import the real component under a different name
import RealSidebar from '../src/app/components/sidebar/sidebar'
// ⬅️ ADDED: wrapper that injects safe defaults for the missing callbacks
const Sidebar = props => (
  <RealSidebar
    onSelectedTimeChange={() => {}}
    onZoneResults={() => {}}
    {...props}
  />
)
import { useRouter } from 'next/router'

jest.mock('next/router')
jest.mock('next/link', () => ({ children, href }) =>
  React.cloneElement(children, { href })
)

jest.mock(
  '../src/app/components/weather-data.js',
  () => ({
    GetWeatherData: () =>
      Promise.resolve({
        list: [
          { readableTime: '2025-07-20T09:00:00', condition: 'Clear', temp: 75 },
          { readableTime: '2025-07-20T15:00:00', condition: 'Clouds', temp: 68 },
        ],
      }),
  }),
  { virtual: true }
)
jest.mock(
  '../src/app/components/useWeather.js',
  () => ({ useWeather: () => ({ icon: '/clear_day.png', temp: 75 }) }),
  { virtual: true }
)

describe('<Sidebar />', () => {
  beforeAll(() => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ data: 'ok' }),
    })
  })

  beforeEach(() => {
    useRouter.mockReturnValue({
      pathname: '/',
      push: jest.fn(),
      prefetch: jest.fn().mockResolvedValue(),
    })
  })

  it('populates date & time from mocked weather data', async () => {
    render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)
    await userEvent.click(screen.getByText(/^Date$/i))
    await userEvent.click(await screen.findByText('July 20'))

    const dateBtn = document.querySelector('.date-dropdown-btn')
    expect(dateBtn).toHaveTextContent(/^July 20$/)

    await userEvent.click(screen.getByText(/^Time$/i))
    await userEvent.click(await screen.findByText('9:00'))

    const timeBtn = document.querySelector('.time-dropdown-btn')
    expect(timeBtn).toHaveTextContent(/^9:00$/)
  })

  it('submit button calls onSubmit and shows the locations list', async () => {
    const mockSubmit = jest.fn()
    const mockSelect = jest.fn()
    const { container } = render(
      <Sidebar onSubmit={mockSubmit} onLocationSelect={mockSelect} />
    )

    await userEvent.click(screen.getByText(/Select Activity/i))
    await userEvent.click(screen.getByText(/Street Photography/i))

    await userEvent.click(screen.getByText(/^Date$/i))
    await userEvent.click(await screen.findByText('July 20'))

    await userEvent.click(screen.getByText(/^Time$/i))
    await userEvent.click(await screen.findByText('9:00'))

    await userEvent.click(screen.getByRole('button', { name: /Submit/i }))
    await waitFor(() => expect(mockSubmit).toHaveBeenCalled())

    const firstLocationName = container.querySelector('.locationName')
    expect(firstLocationName).toHaveTextContent('Washington Square Park')

    await userEvent.click(firstLocationName.closest('.locationItem'))
    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({ zoneName: 'Washington Square Park' })
    )
  })

  it('clears the selected activity when clicking its clear-icon', async () => {
    render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

    await userEvent.click(screen.getByText(/Select Activity/i))
    await userEvent.click(screen.getByText(/Landscape Painting/i))

    const activityClear = document.querySelector('.activityWrapper .clearIcon')
    expect(activityClear).toBeInTheDocument()

    await userEvent.click(activityClear)
    expect(screen.getByText(/Select Activity/i)).toBeVisible()
  })

  it('clears the selected date when clicking its clear-icon', async () => {
    render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

    await userEvent.click(screen.getByText(/^Date$/i))
    await userEvent.click(await screen.findByText('July 20'))

    const dateClear = document.querySelector('.date-dropdown-btn .clearIcon')
    expect(dateClear).toBeInTheDocument()

    await userEvent.click(dateClear)
    expect(screen.getByText(/^Date$/i)).toBeVisible()
  })

  it('clears the selected time when clicking its clear-icon', async () => {
    render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

    await userEvent.click(screen.getByText(/^Date$/i))
    await userEvent.click(await screen.findByText('July 20'))

    await userEvent.click(screen.getByText(/^Time$/i))
    await userEvent.click(await screen.findByText('9:00'))

    const timeClear = document.querySelector('.time-dropdown-btn .clearIcon')
    expect(timeClear).toBeInTheDocument()

    await userEvent.click(timeClear)
    expect(screen.getByText(/^Time$/i)).toBeVisible()
  })
  it('toggles between recommended and manual area panels when slider is clicked', async () => {
    render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)
    expect(screen.queryByText(/financial district/i)).toBeNull()

    const sliderBtn = document.querySelector('.areaToggleBtn')
    await userEvent.click(sliderBtn)

    expect(screen.getByText(/financial district/i)).toBeVisible()
    expect(screen.getByText(/soho hudson square/i)).toBeVisible()

    await userEvent.click(sliderBtn)
    expect(screen.queryByText(/financial district/i)).toBeNull()
    expect(
      screen.getByText(/please submit your choices to view the recommended areas/i)
    ).toBeVisible()
  })

})
