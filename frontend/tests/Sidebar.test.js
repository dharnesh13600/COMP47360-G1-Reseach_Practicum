// import React from 'react'
// import { render, screen, waitFor } from '@testing-library/react'
// import userEvent from '@testing-library/user-event'
// import Sidebar from '../src/app/components/sidebar.js'
// import { useRouter } from 'next/router'

// jest.mock('next/router')
// jest.mock('next/link', () => ({ children, href }) =>
//   React.cloneElement(children, { href })
// )

// jest.mock(
//   '../src/app/components/weather-data.js',
//   () => ({
//     GetWeatherData: () =>
//       Promise.resolve({
//         list: [
//           { readableTime: '2025-07-20T09:00:00', condition: 'Clear', temp: 75 },
//           { readableTime: '2025-07-20T15:00:00', condition: 'Clouds', temp: 68 },
//         ],
//       }),
//   }),
//   { virtual: true }
// )
// jest.mock(
//   '../src/app/components/useWeather.js',
//   () => ({ useWeather: () => ({ icon: '/clear_day.png', temp: 75 }) }),
//   { virtual: true }
// )

// describe('<Sidebar />', () => {
//   beforeAll(() => {
//     global.fetch = jest.fn().mockResolvedValue({
//       ok: true,
//       json: () => Promise.resolve({ data: 'ok' }),
//     })
//   })

//   beforeEach(() => {
//     useRouter.mockReturnValue({
//       pathname: '/',
//       push: jest.fn(),
//       prefetch: jest.fn().mockResolvedValue(),
//     })
//   })

//   it('populates date & time from mocked weather data', async () => {
//     render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

//     // Open date picker and choose July 20
//     await userEvent.click(screen.getByText(/^Date$/i))
//     await userEvent.click(await screen.findByText('July 20'))

//     // The button wrapper has class .date-dropdown-btn
//     const dateBtn = document.querySelector('.date-dropdown-btn')
//     expect(dateBtn).toHaveTextContent(/^July 20$/)

//     // Open time picker and choose 9:00
//     await userEvent.click(screen.getByText(/^Time$/i))
//     await userEvent.click(await screen.findByText('9:00'))

//     const timeBtn = document.querySelector('.time-dropdown-btn')
//     expect(timeBtn).toHaveTextContent(/^9:00$/)
//   })

//   it('submit button calls onSubmit and shows the locations list', async () => {
//     const mockSubmit = jest.fn()
//     const mockSelect = jest.fn()
//     const { container } = render(
//       <Sidebar onSubmit={mockSubmit} onLocationSelect={mockSelect} />
//     )

//     // Fill out activity, date, time
//     await userEvent.click(screen.getByText(/Select Activity/i))
//     await userEvent.click(screen.getByText(/Street Photography/i))

//     await userEvent.click(screen.getByText(/^Date$/i))
//     await userEvent.click(await screen.findByText('July 20'))

//     await userEvent.click(screen.getByText(/^Time$/i))
//     await userEvent.click(await screen.findByText('9:00'))

//     // Submit
//     await userEvent.click(screen.getByRole('button', { name: /Submit/i }))
//     await waitFor(() => expect(mockSubmit).toHaveBeenCalled())

//     // Now recommended locations render as .locationItem > .locationName
//     const firstLocationName = container.querySelector('.locationName')
//     expect(firstLocationName).toHaveTextContent('Washington Square Park')

//     // And clicking it calls onLocationSelect
//     await userEvent.click(firstLocationName.closest('.locationItem'))
//     expect(mockSelect).toHaveBeenCalledWith(
//       expect.objectContaining({ zoneName: 'Washington Square Park' })
//     )
//   })

//   // ─── NEW TEST: clear activity picker ─────────────────────────────────────────
//   it('clears the selected activity when clicking its clear-icon', async () => {
//     render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

//     // select an activity
//     await userEvent.click(screen.getByText(/Select Activity/i))
//     await userEvent.click(screen.getByText(/Landscape Painting/i))

//     // clear icon lives inside .activityWrapper
//     const activityClear = document.querySelector('.activityWrapper .clearIcon')
//     expect(activityClear).toBeInTheDocument()

//     await userEvent.click(activityClear)
//     expect(screen.getByText(/Select Activity/i)).toBeVisible()
//   })
//   // ───────────────────────────────────────────────────────────────────────────────

//   // ─── NEW TEST: clear date picker ────────────────────────────────────────────────
//   it('clears the selected date when clicking its clear-icon', async () => {
//     render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

//     // select a date
//     await userEvent.click(screen.getByText(/^Date$/i))
//     await userEvent.click(await screen.findByText('July 20'))

//     // clear icon lives inside .date-dropdown-btn
//     const dateClear = document.querySelector('.date-dropdown-btn .clearIcon')
//     expect(dateClear).toBeInTheDocument()

//     await userEvent.click(dateClear)
//     expect(screen.getByText(/^Date$/i)).toBeVisible()
//   })
//   // ───────────────────────────────────────────────────────────────────────────────

//   // ─── NEW TEST: clear time picker ────────────────────────────────────────────────
//   it('clears the selected time when clicking its clear-icon', async () => {
//     render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

//     // choose date first so that time list appears
//     await userEvent.click(screen.getByText(/^Date$/i))
//     await userEvent.click(await screen.findByText('July 20'))

//     // select a time
//     await userEvent.click(screen.getByText(/^Time$/i))
//     await userEvent.click(await screen.findByText('9:00'))

//     // clear icon lives inside .time-dropdown-btn
//     const timeClear = document.querySelector('.time-dropdown-btn .clearIcon')
//     expect(timeClear).toBeInTheDocument()

//     await userEvent.click(timeClear)
//     expect(screen.getByText(/^Time$/i)).toBeVisible()
//   })
//   // ───────────────────────────────────────────────────────────────────────────────
// })

//--------------------------------------

// tests/Sidebar.test.js
import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Sidebar from '../src/app/components/sidebar.js'
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

    // Open date picker and choose July 20
    await userEvent.click(screen.getByText(/^Date$/i))
    await userEvent.click(await screen.findByText('July 20'))

    // The button wrapper has class .date-dropdown-btn
    const dateBtn = document.querySelector('.date-dropdown-btn')
    expect(dateBtn).toHaveTextContent(/^July 20$/)

    // Open time picker and choose 9:00
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

    // Fill out activity, date, time
    await userEvent.click(screen.getByText(/Select Activity/i))
    await userEvent.click(screen.getByText(/Street Photography/i))

    await userEvent.click(screen.getByText(/^Date$/i))
    await userEvent.click(await screen.findByText('July 20'))

    await userEvent.click(screen.getByText(/^Time$/i))
    await userEvent.click(await screen.findByText('9:00'))

    // Submit
    await userEvent.click(screen.getByRole('button', { name: /Submit/i }))
    await waitFor(() => expect(mockSubmit).toHaveBeenCalled())

    // Now recommended locations render as .locationItem > .locationName
    const firstLocationName = container.querySelector('.locationName')
    expect(firstLocationName).toHaveTextContent('Washington Square Park')

    // And clicking it calls onLocationSelect
    await userEvent.click(firstLocationName.closest('.locationItem'))
    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({ zoneName: 'Washington Square Park' })
    )
  })

  // ─── NEW TEST: clear activity picker ─────────────────────────────────────────
  it('clears the selected activity when clicking its clear-icon', async () => {
    render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

    // select an activity
    await userEvent.click(screen.getByText(/Select Activity/i))
    await userEvent.click(screen.getByText(/Landscape Painting/i))

    // clear icon lives inside .activityWrapper
    const activityClear = document.querySelector('.activityWrapper .clearIcon')
    expect(activityClear).toBeInTheDocument()

    await userEvent.click(activityClear)
    expect(screen.getByText(/Select Activity/i)).toBeVisible()
  })
  // ───────────────────────────────────────────────────────────────────────────────

  // ─── NEW TEST: clear date picker ────────────────────────────────────────────────
  it('clears the selected date when clicking its clear-icon', async () => {
    render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

    // select a date
    await userEvent.click(screen.getByText(/^Date$/i))
    await userEvent.click(await screen.findByText('July 20'))

    // clear icon lives inside .date-dropdown-btn
    const dateClear = document.querySelector('.date-dropdown-btn .clearIcon')
    expect(dateClear).toBeInTheDocument()

    await userEvent.click(dateClear)
    expect(screen.getByText(/^Date$/i)).toBeVisible()
  })
  // ───────────────────────────────────────────────────────────────────────────────

  // ─── NEW TEST: clear time picker ────────────────────────────────────────────────
  it('clears the selected time when clicking its clear-icon', async () => {
    render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

    // choose date first so that time list appears
    await userEvent.click(screen.getByText(/^Date$/i))
    await userEvent.click(await screen.findByText('July 20'))

    // select a time
    await userEvent.click(screen.getByText(/^Time$/i))
    await userEvent.click(await screen.findByText('9:00'))

    // clear icon lives inside .time-dropdown-btn
    const timeClear = document.querySelector('.time-dropdown-btn .clearIcon')
    expect(timeClear).toBeInTheDocument()

    await userEvent.click(timeClear)
    expect(screen.getByText(/^Time$/i)).toBeVisible()
  })
  // ────────────────────────────────────────────────────────────────────────────────

  // ─── NEW TEST: slider toggle ───────────────────────────────────────────────────
  it('toggles between recommended and manual area panels when slider is clicked', async () => {
    render(<Sidebar onSubmit={jest.fn()} onLocationSelect={jest.fn()} />)

    // 1) Initially, manual areas hidden
    expect(screen.queryByText(/financial district/i)).toBeNull()

    // 2) Click the slider button (.areaToggleBtn)
    const sliderBtn = document.querySelector('.areaToggleBtn')
    await userEvent.click(sliderBtn)

    // Should display manual neighborhood names
    expect(screen.getByText(/financial district/i)).toBeVisible()
    expect(screen.getByText(/soho hudson square/i)).toBeVisible()

    // 3) Click again to go back
    await userEvent.click(sliderBtn)
    expect(screen.queryByText(/financial district/i)).toBeNull()
    expect(
      screen.getByText(/please submit your choices to view the recommended areas/i)
    ).toBeVisible()
  })
  // ────────────────────────────────────────────────────────────────────────────────
})
