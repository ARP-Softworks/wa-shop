/** Reference data to pre-populate model/capacity/color pickers in the admin product form.
 *  All fields stay free-text underneath (datalist, not a closed select) so an unlisted
 *  model/color typed by hand is never blocked. */

export const IPHONE_MODELS: string[] = [
  'iPhone 8',
  'iPhone 8 Plus',
  'iPhone X',
  'iPhone XR',
  'iPhone XS',
  'iPhone XS Max',
  'iPhone 11',
  'iPhone 11 Pro',
  'iPhone 11 Pro Max',
  'iPhone SE (2020)',
  'iPhone 12 mini',
  'iPhone 12',
  'iPhone 12 Pro',
  'iPhone 12 Pro Max',
  'iPhone 13 mini',
  'iPhone 13',
  'iPhone 13 Pro',
  'iPhone 13 Pro Max',
  'iPhone SE (2022)',
  'iPhone 14',
  'iPhone 14 Plus',
  'iPhone 14 Pro',
  'iPhone 14 Pro Max',
  'iPhone 15',
  'iPhone 15 Plus',
  'iPhone 15 Pro',
  'iPhone 15 Pro Max',
  'iPhone 16',
  'iPhone 16 Plus',
  'iPhone 16 Pro',
  'iPhone 16 Pro Max',
  'iPhone 16e',
  'iPhone Air',
  'iPhone 17',
  'iPhone 17 Pro',
  'iPhone 17 Pro Max',
];

export const IPHONE_CAPACITIES: string[] = ['64GB', '128GB', '256GB', '512GB', '1TB', '2TB'];

const DEFAULT_COLORS = ['Negro', 'Blanco'];

export const IPHONE_MODEL_COLORS: Record<string, string[]> = {
  'iPhone 8': ['Gris espacial', 'Plata', 'Dorado', '(PRODUCT)RED'],
  'iPhone 8 Plus': ['Gris espacial', 'Plata', 'Dorado', '(PRODUCT)RED'],
  'iPhone X': ['Gris espacial', 'Plata'],
  'iPhone XR': ['Blanco', 'Negro', 'Azul', 'Amarillo', 'Coral', '(PRODUCT)RED'],
  'iPhone XS': ['Gris espacial', 'Plata', 'Dorado'],
  'iPhone XS Max': ['Gris espacial', 'Plata', 'Dorado'],
  'iPhone 11': ['Negro', 'Blanco', 'Verde', 'Amarillo', 'Púrpura', '(PRODUCT)RED'],
  'iPhone 11 Pro': ['Verde medianoche', 'Gris espacial', 'Plata', 'Dorado'],
  'iPhone 11 Pro Max': ['Verde medianoche', 'Gris espacial', 'Plata', 'Dorado'],
  'iPhone SE (2020)': ['Negro', 'Blanco', '(PRODUCT)RED'],
  'iPhone 12 mini': ['Negro', 'Blanco', 'Azul', 'Verde', 'Púrpura', '(PRODUCT)RED'],
  'iPhone 12': ['Negro', 'Blanco', 'Azul', 'Verde', 'Púrpura', '(PRODUCT)RED'],
  'iPhone 12 Pro': ['Grafito', 'Plata', 'Dorado', 'Azul pacífico'],
  'iPhone 12 Pro Max': ['Grafito', 'Plata', 'Dorado', 'Azul pacífico'],
  'iPhone 13 mini': ['Rosa', 'Azul', 'Medianoche', 'Blanco estelar', '(PRODUCT)RED', 'Verde'],
  'iPhone 13': ['Rosa', 'Azul', 'Medianoche', 'Blanco estelar', '(PRODUCT)RED', 'Verde'],
  'iPhone 13 Pro': ['Grafito', 'Plata', 'Dorado', 'Azul sierra', 'Verde alpino'],
  'iPhone 13 Pro Max': ['Grafito', 'Plata', 'Dorado', 'Azul sierra', 'Verde alpino'],
  'iPhone SE (2022)': ['Medianoche', 'Blanco estelar', '(PRODUCT)RED'],
  'iPhone 14': ['Azul', 'Púrpura', 'Medianoche', 'Blanco estelar', '(PRODUCT)RED', 'Amarillo'],
  'iPhone 14 Plus': ['Azul', 'Púrpura', 'Medianoche', 'Blanco estelar', '(PRODUCT)RED', 'Amarillo'],
  'iPhone 14 Pro': ['Negro espacial', 'Plata', 'Dorado', 'Púrpura oscuro'],
  'iPhone 14 Pro Max': ['Negro espacial', 'Plata', 'Dorado', 'Púrpura oscuro'],
  'iPhone 15': ['Negro', 'Azul', 'Verde', 'Amarillo', 'Rosa'],
  'iPhone 15 Plus': ['Negro', 'Azul', 'Verde', 'Amarillo', 'Rosa'],
  'iPhone 15 Pro': ['Titanio negro', 'Titanio blanco', 'Titanio azul', 'Titanio natural'],
  'iPhone 15 Pro Max': ['Titanio negro', 'Titanio blanco', 'Titanio azul', 'Titanio natural'],
  'iPhone 16': ['Negro', 'Blanco', 'Rosa', 'Verde azulado', 'Ultramarino'],
  'iPhone 16 Plus': ['Negro', 'Blanco', 'Rosa', 'Verde azulado', 'Ultramarino'],
  'iPhone 16 Pro': ['Titanio negro', 'Titanio blanco', 'Titanio natural', 'Titanio desierto'],
  'iPhone 16 Pro Max': ['Titanio negro', 'Titanio blanco', 'Titanio natural', 'Titanio desierto'],
  'iPhone 16e': ['Negro', 'Blanco'],
  'iPhone Air': ['Negro espacial', 'Blanco nube', 'Azul cielo', 'Dorado claro'],
  'iPhone 17': ['Negro', 'Blanco', 'Lavanda', 'Salvia', 'Azul bruma'],
  'iPhone 17 Pro': ['Plateado', 'Naranja cósmico', 'Azul profundo'],
  'iPhone 17 Pro Max': ['Plateado', 'Naranja cósmico', 'Azul profundo'],
};

export function colorsForModel(model: string | null | undefined): string[] {
  if (!model) {
    return Array.from(new Set(Object.values(IPHONE_MODEL_COLORS).flat())).sort();
  }
  return IPHONE_MODEL_COLORS[model] ?? DEFAULT_COLORS;
}
