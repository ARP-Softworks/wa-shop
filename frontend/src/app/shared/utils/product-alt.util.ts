import { ProductCondition } from '../models/catalog.models';

export function productAlt(product: {
  name: string;
  storageCapacity?: string | null;
  condition: ProductCondition;
  color?: string | null;
}): string {
  const parts = [product.name];
  if (product.storageCapacity) {
    parts.push(product.storageCapacity);
  }
  parts.push(product.condition === 'NEW' ? 'nuevo' : 'usado');
  if (product.color) {
    parts.push(product.color);
  }
  return parts.filter(Boolean).join(' ');
}

/** Converts a URL model slug (e.g. iphone-14) into a model search term. */
export function modelFromSlug(modelSlug: string): string {
  return decodeURIComponent(modelSlug).replace(/-/g, ' ').trim();
}
