process.env.NEXT_PUBLIC_USE_MAPBOX = 'true'
jest.mock('mapbox-gl', () => {
  const flyToMock = jest.fn()
  const onMock = jest.fn()

  class MockMap {
    constructor() {
      return {
        flyTo: flyToMock,
        on: onMock,
        addControl: jest.fn(),
        resize: jest.fn(),
        remove: jest.fn(),
      }
    }
  }

  return {
    __esModule: true,
    default: {
      Map: MockMap,
      NavigationControl: class {},
      Popup: class {},
      Marker: class {},
    },
    flyToMock,
    onMock,
  }
})


const mapboxgl = require('mapbox-gl')
const { default: Map } = require('../src/app/components/mapComponent/map')

const { flyToMock, onMock } = mapboxgl

import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

describe('<Map/> Reset View', () => {
  beforeEach(() => {
    flyToMock.mockClear()
    onMock.mockClear()
  })

  it('renders the Reset View button and resets map view on click', async () => {
    render(<Map />)

    expect(onMock).toHaveBeenCalledWith('load', expect.any(Function))

    const btn = screen.getByRole('button', { name: /reset view/i })
    expect(btn).toBeInTheDocument()

    await userEvent.click(btn)

    expect(flyToMock).toHaveBeenCalledWith({
      center: [-74.0, 40.7526],
      zoom: 11.57,
      pitch: 0,
      bearing: 0,
      duration: 3000,
    })
  })
})
