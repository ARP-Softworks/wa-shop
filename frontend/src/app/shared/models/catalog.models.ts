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
  currency: CurrencyCode;
  stock: number;
  warranty: string | null;
  featured: boolean;
  primaryImageUrl: string | null;
  createdAt: string;
}

export interface ProductDetail extends ProductSummary {
  description: string | null;
  published: boolean;
  categoryId: string | null;
  categoryName: string | null;
  images: ProductImage[];
  features: ProductFeature[];
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
}

export interface OrderCreateRequest {
  customerName: string;
  customerPhone: string;
  customerEmail?: string | null;
  shippingAddress?: string | null;
  items: { productId: string; quantity: number }[];
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
