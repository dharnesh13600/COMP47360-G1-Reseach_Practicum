const nextJest = require('next/jest');
const createJestConfig = nextJest({ dir: './' });

/** @type {import('jest').Config} */
const custom = {
  // 1. Use JSDOM for a browser-like environment
  testEnvironment: 'jsdom',

  // 2. Load our setup file after the test framework is installed
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],

  // 3. Stub out CSS and other static assets
  moduleNameMapper: {
    '\\.(css|less|scss|sass)$': '<rootDir>/__mocks__/styleMock.js',
  },

  // 4. Look for test files under the `tests/` directory
  testMatch: ['<rootDir>/tests/**/*.test.js'],
};

module.exports = createJestConfig(custom);

