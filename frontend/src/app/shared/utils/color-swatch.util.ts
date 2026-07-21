/** Best-effort color-name → hex map for rendering small swatch dots on product cards.
 *  Names are free text entered by the admin, so this is a lookup, not an exhaustive spec —
 *  unmapped names fall back to a neutral gray dot (still useful as a "N colors" indicator). */
const COLOR_HEX_MAP: Record<string, string> = {
  // Neutrals / metals
  negro: '#1c1c1e',
  black: '#1c1c1e',
  blanco: '#f5f5f7',
  white: '#f5f5f7',
  starlight: '#f0e5d6',
  medianoche: '#1c1c1e',
  midnight: '#1c1c1e',
  plata: '#e3e4e5',
  silver: '#e3e4e5',
  grafito: '#4b4b4d',
  graphite: '#4b4b4d',
  'gris espacial': '#5f5f5f',
  'space gray': '#5f5f5f',
  'space grey': '#5f5f5f',
  oro: '#f0e0c9',
  gold: '#f0e0c9',
  // Titanium finishes (iPhone 15/16 Pro)
  'titanio natural': '#8f8a7f',
  'natural titanium': '#8f8a7f',
  'titanio azul': '#3e4a5b',
  'blue titanium': '#3e4a5b',
  'titanio blanco': '#f2f1ec',
  'white titanium': '#f2f1ec',
  'titanio negro': '#3c3c3c',
  'black titanium': '#3c3c3c',
  'titanio desierto': '#b8a48a',
  'desert titanium': '#b8a48a',
  // Blues
  azul: '#2f5fa8',
  blue: '#2f5fa8',
  'azul profundo': '#2c4a6e',
  'deep blue': '#2c4a6e',
  'azul cielo': '#a9c6de',
  'sky blue': '#a9c6de',
  'ultramarine': '#5271ff',
  ultramarino: '#5271ff',
  // Greens
  verde: '#4b6b4f',
  green: '#4b6b4f',
  'verde alpino': '#4b5a49',
  'alpine green': '#4b5a49',
  // Purples/pinks
  morado: '#8b7b9e',
  purple: '#8b7b9e',
  'purpura': '#8b7b9e',
  rosa: '#f2c6ce',
  pink: '#f2c6ce',
  // Reds/yellows/oranges
  rojo: '#a72030',
  red: '#a72030',
  amarillo: '#f0d97a',
  yellow: '#f0d97a',
  naranja: '#d9743a',
  orange: '#d9743a',
  coral: '#ff7f6b',
  // Teal (iPhone 16)
  teal: '#4f7a78',
  'verde azulado': '#4f7a78',
};

// Longest keys first so "titanio natural" wins over a looser "azul" contained elsewhere.
const SORTED_KEYS = Object.keys(COLOR_HEX_MAP).sort((a, b) => b.length - a.length);

const FALLBACK_HEX = '#cbd5e1';

export function colorToHex(colorName: string | null | undefined): string {
  if (!colorName) {
    return FALLBACK_HEX;
  }
  const normalized = colorName.trim().toLowerCase();
  if (COLOR_HEX_MAP[normalized]) {
    return COLOR_HEX_MAP[normalized];
  }
  // Admin-entered names often add qualifiers ("Naranja cósmico", "Cosmic Orange") around a
  // recognizable base color word — match on containment instead of requiring an exact hit.
  const match = SORTED_KEYS.find((key) => normalized.includes(key));
  return match ? COLOR_HEX_MAP[match] : FALLBACK_HEX;
}
