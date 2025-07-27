export const push = jest.fn();
export const replace = jest.fn();

export function createMockRouter() {
  return {
    push,
    replace,
    prefetch: jest.fn().mockResolvedValue(undefined),
    pathname: '/',
    query: {},
    asPath: '/',
  };
}