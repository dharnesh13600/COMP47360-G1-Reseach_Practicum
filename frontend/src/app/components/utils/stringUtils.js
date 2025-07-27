export function sanitizeName(str) {
  return str
    .replace(/[:,]/g, ' ')
    .replace(/\b(Manhattan|New York|And|Between)\b/gi, '')
    .replace(/\s{2,}/g, ' ')
    .trim();
}

export function cleanAndTruncate(str, n = 3) {
  const cleaned = sanitizeName(str);
  const words = cleaned.split(/\s+/);
  return words.length <= n
    ? cleaned
    : words.slice(0, n).join(' ');
}
