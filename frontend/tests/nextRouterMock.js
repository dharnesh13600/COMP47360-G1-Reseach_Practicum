export const push = jest.fn();
export const replace = jest.fn();
export function createMockRouter() {
  return { push, replace, pathname: '/', query: {}, asPath: '/', prefetch: jest.fn() };
}