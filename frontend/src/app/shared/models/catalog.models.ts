export type ProductCondition = 'NEW' | 'USED';
export type ProductType = 'IPHONE' | 'ACCESSORY';
export type CurrencyCode = 'UYU' | 'USD';

export interface ProductImage {
  id: string;
  url: string;
  altText: string | null;
  position: number;
  mainImage: boolean;
  width?: number | null;
  height?: number | null;
}

export interface ProductFeature {
  id: string;
  name: string;
  value: string;
}

export interface ProductVariantPublic {
  id: string;
  condition: ProductCondition;
  storageCapacity: string | null;
  color: string | null;
  batteryHealth: number | null;
  price: number;
  previousPrice: number | null;
  currency: CurrencyCode;
  stock: number;
  warranty: string | null;
  published: boolean;
  primaryImageUrl: string | null;
  images: ProductImage[];
}

export interface ProductSummary {
  id: string;
  slug: string;
  name: string;
  model: string | null;
  productType: ProductType;
  condition: ProductCondition;
  storageCapacity: string | null;
  color: string | null;
  batteryHealth: number | null;
  price: number;
  previousPrice: number | null;
  promoBuyQuantity: number | null;
  promoPayQuantity: number | null;
  currency: CurrencyCode;
  stock: number;
  warranty: string | null;
  featured: boolean;
  primaryImageUrl: string | null;
  createdAt: string;
  categoryId: string | null;
}

export interface ProductDetail extends ProductSummary {
  description: string | null;
  published: boolean;
  categoryId: string | null;
  categoryName: string | null;
  variants: ProductVariantPublic[];
  features: ProductFeature[];
  compatibleModels: string[];
  seoTitle?: string | null;
  metaDescription?: string | null;
  indexable?: boolean;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ProductSearchParams {
  q?: string;
  productType?: ProductType;
  condition?: ProductCondition;
  model?: string;
  storageCapacity?: string;
  color?: string;
  minBatteryHealth?: number;
  minPrice?: number;
  maxPrice?: number;
  featured?: boolean;
  inStock?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  active: boolean;
  seoTitle?: string | null;
  metaDescription?: string | null;
  indexable?: boolean;
}

export interface TechnicalServiceItem {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  price: number | null;
  currency: CurrencyCode | null;
  estimatedTime: string | null;
  active: boolean;
  seoTitle?: string | null;
  metaDescription?: string | null;
  indexable?: boolean;
}

export interface PublicSiteSettings {
  businessName: string;
  whatsappNumber: string | null;
  instagramUrl: string | null;
  address: string | null;
  openingHours: string | null;
  contactEmail: string | null;
  logoUrl: string | null;
  publicSiteUrl?: string | null;
  defaultSeoTitle?: string | null;
  defaultMetaDescription?: string | null;
  defaultSocialImageUrl?: string | null;
  googleSiteVerification?: string | null;
  bingSiteVerification?: string | null;
  country?: string | null;
  region?: string | null;
  city?: string | null;
  postalCode?: string | null;
  mercadoPagoPublicKey?: string | null;
}

export interface CartItem {
  variantId: string;
  productId: string;
  slug: string;
  name: string;
  price: number;
  currency: CurrencyCode;
  primaryImageUrl: string | null;
  storageCapacity: string | null;
  color: string | null;
  condition: ProductCondition;
  productType: ProductType;
  stock: number;
  quantity: number;
  promoBuyQuantity: number | null;
  promoPayQuantity: number | null;
  categoryId: string | null;
}

/** "Buy X pay Y" line pricing (2x1, 3x2...) — mirrors CheckoutService.calculateLineSubtotal on the backend. */
export function calculateLineSubtotal(
  unitPrice: number,
  quantity: number,
  promoBuyQuantity: number | null,
  promoPayQuantity: number | null
): number {
  if (!promoBuyQuantity || !promoPayQuantity || promoBuyQuantity <= promoPayQuantity) {
    return unitPrice * quantity;
  }
  const bundles = Math.floor(quantity / promoBuyQuantity);
  const remainder = quantity % promoBuyQuantity;
  const payableUnits = bundles * promoPayQuantity + remainder;
  return unitPrice * payableUnits;
}

/** Cross-category "buy N from group A, get M from group B at X% off" promotion (mirrors backend PromotionEngine). */
export interface PublicPromotion {
  triggerCategoryId: string;
  triggerQuantity: number;
  rewardCategoryId: string;
  rewardQuantity: number;
  discountPercent: number;
}

interface CategoryCartLine {
  key: string;
  categoryId: string;
  quantity: number;
  effectiveUnitPrice: number;
}

/** Same-category trigger/reward covers mix-and-match; different categories cover cross-sell gifts. */
export function calculatePromotionDiscounts(
  lines: CategoryCartLine[],
  promotions: PublicPromotion[]
): Map<string, number> {
  const discounts = new Map<string, number>();
  for (const promotion of promotions) {
    const triggerUnits = lines
      .filter((line) => line.categoryId === promotion.triggerCategoryId)
      .reduce((sum, line) => sum + line.quantity, 0);
    const multiplier = Math.floor(triggerUnits / promotion.triggerQuantity);
    if (multiplier === 0) {
      continue;
    }

    let rewardUnitsRemaining = multiplier * promotion.rewardQuantity;
    const discountFraction = promotion.discountPercent / 100;
    const rewardLines = lines
      .filter((line) => line.categoryId === promotion.rewardCategoryId)
      .slice()
      .sort((a, b) => a.effectiveUnitPrice - b.effectiveUnitPrice);

    for (const line of rewardLines) {
      if (rewardUnitsRemaining <= 0) {
        break;
      }
      const unitsHere = Math.min(rewardUnitsRemaining, line.quantity);
      const lineDiscount = Math.round(line.effectiveUnitPrice * unitsHere * discountFraction * 100) / 100;
      discounts.set(line.key, (discounts.get(line.key) ?? 0) + lineDiscount);
      rewardUnitsRemaining -= unitsHere;
    }
  }
  return discounts;
}

export function totalPromotionDiscount(discounts: Map<string, number>): number {
  let total = 0;
  for (const value of discounts.values()) {
    total += value;
  }
  return total;
}

export function buildCategoryCartLines(items: CartItem[]): CategoryCartLine[] {
  return items
    .filter((item) => item.categoryId != null)
    .map((item) => ({
      key: item.variantId,
      categoryId: item.categoryId as string,
      quantity: item.quantity,
      effectiveUnitPrice: calculateLineSubtotal(item.price, item.quantity, item.promoBuyQuantity, item.promoPayQuantity) / item.quantity,
    }));
}

export interface PublicHeroBanner {
  id: string;
  imageUrl: string;
  altText: string | null;
  linkUrl: string | null;
}

export interface OrderCreateRequest {
  customerName: string;
  customerPhone: string;
  customerEmail?: string | null;
  shippingAddress?: string | null;
  discountCode?: string | null;
  items: { variantId: string; quantity: number }[];
}

export interface OrderCreateResponse {
  orderId: string;
  checkoutUrl: string;
}

export type OrderStatus =
  | 'PENDING_PAYMENT'
  | 'PAID'
  | 'CONFIRMED'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'REJECTED'
  | 'EXPIRED';

export interface OrderStatusResponse {
  orderId: string;
  status: OrderStatus;
}
