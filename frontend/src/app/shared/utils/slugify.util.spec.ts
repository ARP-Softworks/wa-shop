import { slugify } from './slugify.util';

describe('slugify', () => {
  it('converts text to lowercase kebab-case', () => {
    expect(slugify('iPhone 15 Pro Max')).toBe('iphone-15-pro-max');
  });

  it('removes accents and trims separators', () => {
    expect(slugify('  Camión Eléctrico!!  ')).toBe('camion-electrico');
  });

  it('returns empty string for blank input', () => {
    expect(slugify('   ')).toBe('');
  });
});
