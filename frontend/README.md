# **Next.js folder structure Documentation**

## Table of Contents

- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Admin vs Public Site](#admin-vs-public-site)
- [Styling](#styling)
- [Hooks And Shared Logic](#hooks-and-shared-logic)
- [Scripts](#scripts)
- [Testing](#testing)
- [API Integration](#api-integration)
- [Troubleshooting](#troubleshooting)

## Getting Started

Before running this project, make sure to have:
- Node.js (version 18.x or higher)
- npm/yarn/pnpm/bun
- Git
- Manhattan Muse Backend API running

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

## Project Structure

```
src/
├── app/                                    # Next.js App Router entries
│   ├── (site)/                             # Public-facing site (about, map, home etc.)
│   │   ├── layout.js                       # Public site layout
│   │   ├── page.js                         # Homepage of public site
│   │   ├── map/                            # Interactive map page
│   │   ├── about/                          # About page
│   │   └── global.css                      # Global styles
│   ├── admin/                              # Admin dashboard with its own layout and pages
│   │   ├── layout.js                       # Admin layout
│   │   ├── page.js                         # Admin dashboard
│   │   ├── dashboard/                      # Analytics pages
│   |__ api/                                # API routes and server actions
|   |__ components/                         # Shared React components of public site
│       ├── aboutComponent/                 # About components
│       ├── dropdowns/                      # Dropdown components
│       ├── homeComponent/                  # Home related components
|       |── mapComponent/                   # Map-related components
|       |──sidebar/                         # sidebar components
|       |──utils/                           # helper components
|       └── hooks/                          # Custom React hooks
│           ├── useActivities.js            # Activities data fetching
│           ├── useDateTimes.js             # DateTime management
│           ├── useRecommendations.js       # Recommendations logic
│           |__ useWeatherData.js            # Weather data integration
|       |__styles/                          # styles of all components of public site
├── components/admin/                       # Admin-specific components
├── helper/                                 # UI helpers for public site
├── lib/                                    # API client and shared logic
└── utils/                                  # General utilities for admin site

---

```
## Admin vs Public Site

The folder structure separates the admin interface (/admin) from the public site (/(site)) via Next.js layouts. 

**Public Site ((site) /)**- Open to all users, includes homepage, map and about pages.

**Admin Dashboard (admin/)**: Protected area for administrators with analytics, user management and settings

Ensure any access control or authentication logic is implemented for admin routes.
## Environment Variables:

Create a .env.local file in the src directory:

NEXT_PUBLIC_USE_MAPBOX=true
NEXT_PUBLIC_MAPBOX_TOKEN=mapbox token
NEXT_PUBLIC_MAPBOX_STYLE_URL=your mapbox style url
NEXT_PUBLIC_MAPBOX_STYLE_DARK_URL=mapbox dark theme url
NEXT_PUBLIC_GOOGLE_TOKEN=google api token


NEXT_PUBLIC_BACKEND_API_URL=backend api url
## Styling

Combination of Vanilla CSS and CSS Modules for component-based styling and global.css for global styles.
## Hooks And Shared Logic

Custom hooks such as useActivities, useDateTimes, useRecommendations, useWeatherData in (app/components/hooks) to encapsulate data fetching and state management patterns used across pages.
## Scripts
```
npm run dev     -    Start development server with Turbopack
npm run build   -    Build for production
npm start       -    Start production server
npm run lint    -    Run ESLint
npm test        -    Run unit tests

```
## Testing
```
npm test
```
## API Integration

    The frontend communicates with the Manhattan Muse Backend API through /app/components/utils/apiHelpers. Key endpoints include:
    - **Recommendations**   :   GET /api/recommendations
    - **Activities**        :   GET /api/activities
    - **Date Times**        :   GET /api/forecast/available-datetimes
    - **Weather**           :   GET api/weather
## Troubleshooting

    - API errors: Confirm NEXT_PUBLIC_BACKEND_API_URL is correct and the backend is reachable. Check console logs from lib/api.js for   request/response details.

    - Missing styles or broken widgets: Verify the helper modules and corresponding .css files are imported where used.

    - Routing issues: Ensure the App Router conventions (layout.js, page.js, route grouping) are respected.
## Contributing

    Fork or branch the repository.

    Add/modify source under src/.

    Run linting/tests and ensure they pass.






