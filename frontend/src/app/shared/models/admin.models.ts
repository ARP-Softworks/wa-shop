import {
  CurrencyCode,
  OrderStatus,
  PageResponse,
  ProductCondition,
  ProductFeature,
  ProductType,
} from './catalog.models';

export type { PageResponse, OrderStatus };

export type InquiryStatus = 'NEW' | 'IN_PROGRESS' | 'RESPONDED' | 'CLOSED';
export type InquirySource = 'WHATSAPP_CLICK' | 'FORM' | 'OTHER';
export type AuditAction =
  | 'CREATE'
  | 'UPDATE'
  | 'DELETE'
  | 'PUBLISH'
  | 'UNPUBLISH'
  | 'LOGIN'
  | 'OTHER';

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}

/** Admin image projection includes storage metadata (not exposed on public catalog DTOs). */
export interface AdminProductImage {
  id: string;
  url: string;
  publicId: string | null;
  altText: string | null;
  position: number;
  mainImage: boolean;
  format?: string | null;
  sizeBytes?: number | null;
  width?: number | null;
  height?: number | null;
}

export interface AdminProductSummary {
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
  promoBuyQuantity: number | null;
  promoPayQuantity: number | null;
  currency: CurrencyCode;
  stock: number;
  imei: string | null;
  published: boolean;
  featured: boolean;
  primaryImageUrl: string | null;
  updatedAt: string;
}

export interface AdminProductDetail {
  id: string;
  slug: string;
  name: string;
  model: string | null;
  description: string | null;
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
  imei: string | null;
  published: boolean;
  featured: boolean;
  categoryId: string | null;
  categoryName: string | null;
  images: AdminProductImage[];
  features: ProductFeature[];
  seoTitle?: string | null;
  metaDescription?: string | null;
  indexable?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductFeatureWrite {
  name: string;
  value: string;
}

export interface ProductImageWrite {
  url: string;
  publicId?: string | null;
  altText?: string | null;
  position: number;
  mainImage: boolean;
  format?: string | null;
  sizeBytes?: number | null;
  width?: number | null;
  height?: number | null;
}

export interface ProductWriteRequest {
  name: string;
  slug: string;
  model?: string | null;
  description?: string | null;
  productType: ProductType;
  condition: ProductCondition;
  storageCapacity?: string | null;
  color?: string | null;
  batteryHealth?: number | null;
  price: number;
  previousPrice?: number | null;
  promoBuyQuantity?: number | null;
  promoPayQuantity?: number | null;
  currency: CurrencyCode;
  stock: number;
  warranty?: string | null;
  imei?: string | null;
  published: boolean;
  featured: boolean;
  categoryId?: string | null;
  seoTitle?: string | null;
  metaDescription?: string | null;
  indexable?: boolean;
  features: ProductFeatureWrite[];
  images: ProductImageWrite[];
}

export interface AdminProductSearchParams {
  q?: string;
  productType?: ProductType;
  condition?: ProductCondition;
  published?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

export interface AdminCategory {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  active: boolean;
  seoTitle?: string | null;
  metaDescription?: string | null;
  indexable?: boolean;
}

export interface CategoryWriteRequest {
  name: string;
  slug: string;
  description?: string | null;
  active: boolean;
  seoTitle?: string | null;
  metaDescription?: string | null;
  indexable?: boolean;
}

export interface AdminTechnicalService {
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

export interface TechnicalServiceWriteRequest {
  name: string;
  slug: string;
  description?: string | null;
  price?: number | null;
  currency?: CurrencyCode | null;
  estimatedTime?: string | null;
  active: boolean;
  seoTitle?: string | null;
  metaDescription?: string | null;
  indexable?: boolean;
}

export interface AdminInquirySummary {
  id: string;
  customerName: string;
  phone: string | null;
  email: string | null;
  status: InquiryStatus;
  source: InquirySource;
  productId: string | null;
  productName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface InquiryStatusHistory {
  id: string;
  fromStatus: InquiryStatus | null;
  toStatus: InquiryStatus;
  changedBy: string | null;
  note: string | null;
  createdAt: string;
}

export interface AdminInquiryDetail {
  id: string;
  customerName: string;
  phone: string | null;
  email: string | null;
  message: string | null;
  adminNotes: string | null;
  status: InquiryStatus;
  source: InquirySource;
  productId: string | null;
  productName: string | null;
  productSlug: string | null;
  createdAt: string;
  updatedAt: string;
  statusHistory: InquiryStatusHistory[];
}

export interface InquiryStatusUpdateRequest {
  status: InquiryStatus;
  note?: string | null;
  adminNotes?: string | null;
}

export interface AdminInquirySearchParams {
  status?: InquiryStatus;
  page?: number;
  size?: number;
}

export interface SiteSettings {
  id: string;
  businessName: string;
  whatsappNumber: string | null;
  instagramUrl: string | null;
  address: string | null;
  openingHours: string | null;
  contactEmail: string | null;
  logoUrl: string | null;
  logoPublicId: string | null;
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
  createdAt: string;
  updatedAt: string;
}

export interface SiteSettingsWriteRequest {
  businessName: string;
  whatsappNumber?: string | null;
  instagramUrl?: string | null;
  address?: string | null;
  openingHours?: string | null;
  contactEmail?: string | null;
  logoUrl?: string | null;
  logoPublicId?: string | null;
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
}

export interface MediaUploadResponse {
  url: string;
  publicId: string;
  provider: string;
  format: string | null;
  contentType: string | null;
  sizeBytes: number;
  width: number | null;
  height: number | null;
}

export interface DashboardResponse {
  publishedProducts: number;
  newDevices: number;
  usedDevices: number;
  pendingInquiries: number;
  pendingOrders: number;
  activeTechnicalServices: number;
  recentProducts: AdminProductSummary[];
  recentInquiries: AdminInquirySummary[];
  activeServices: AdminTechnicalService[];
}

export interface AdminOrderSummary {
  id: string;
  customerId: string;
  customerName: string | null;
  customerPhone: string | null;
  status: OrderStatus;
  total: number;
  currency: CurrencyCode;
  itemCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItemDetail {
  id: string;
  productId: string | null;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface OrderStatusHistoryEntry {
  id: string;
  fromStatus: OrderStatus | null;
  toStatus: OrderStatus;
  changedBy: string | null;
  note: string | null;
  createdAt: string;
}

export interface AdminOrderDetail {
  id: string;
  customerId: string;
  customerName: string | null;
  customerPhone: string | null;
  customerEmail: string | null;
  status: OrderStatus;
  subtotal: number;
  total: number;
  currency: CurrencyCode;
  shippingAddress: string | null;
  mpPreferenceId: string | null;
  mpPaymentId: string | null;
  mpPaymentStatus: string | null;
  notes: string | null;
  items: OrderItemDetail[];
  statusHistory: OrderStatusHistoryEntry[];
  createdAt: string;
  updatedAt: string;
}

export interface OrderStatusUpdateRequest {
  status: OrderStatus;
  note?: string | null;
}

export interface AdminOrderSearchParams {
  status?: OrderStatus;
  customerId?: string;
  page?: number;
  size?: number;
}

export interface AdminCustomer {
  id: string;
  name: string;
  phone: string;
  email: string | null;
  address: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerWriteRequest {
  name: string;
  phone: string;
  email?: string | null;
  address?: string | null;
  notes?: string | null;
}

export interface AdminCustomerSearchParams {
  q?: string;
  page?: number;
  size?: number;
}

export interface AdminUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserWriteRequest {
  email: string;
  password?: string | null;
  firstName: string;
  lastName: string;
  enabled: boolean;
}

export interface AdminUserSearchParams {
  q?: string;
  page?: number;
  size?: number;
}

export interface AdminPromotion {
  id: string;
  name: string;
  active: boolean;
  triggerCategoryId: string;
  triggerCategoryName: string;
  triggerQuantity: number;
  rewardCategoryId: string;
  rewardCategoryName: string;
  rewardQuantity: number;
  discountPercent: number;
  createdAt: string;
  updatedAt: string;
}

export interface PromotionWriteRequest {
  name: string;
  active: boolean;
  triggerCategoryId: string;
  triggerQuantity: number;
  rewardCategoryId: string;
  rewardQuantity: number;
  discountPercent: number;
}

export interface AuditLog {
  id: string;
  userId: string | null;
  action: AuditAction;
  entityType: string;
  entityId: string | null;
  details: string | null;
  createdAt: string;
}
