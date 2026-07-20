import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import {
  CartItem,
  ProductDetail,
  ProductVariantPublic,
  PublicPromotion,
  buildCategoryCartLines,
  calculateLineSubtotal,
  calculatePromotionDiscounts,
  totalPromotionDiscount,
} from '../../shared/models/catalog.models';
import { PublicContentApiService } from '../api/public-content-api.service';
import { PublicDiscountCodeApiService } from '../api/public-discount-code-api.service';

const STORAGE_KEY = 'wa-shop-cart-v2';
const COUPON_STORAGE_KEY = 'wa-shop-coupon-v1';
/** Keep in sync with backend OrderItemRequest @Max. */
const MAX_LINE_QUANTITY = 10;

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly contentApi = inject(PublicContentApiService);
  private readonly discountApi = inject(PublicDiscountCodeApiService);
  private readonly items = signal<CartItem[]>(this.readFromStorage());
  private readonly promotions = signal<PublicPromotion[]>([]);
  private readonly appliedCouponCode = signal<string | null>(null);
  private readonly appliedCouponAmount = signal(0);

  readonly cartItems = this.items.asReadonly();
  readonly itemCount = computed(() => this.items().reduce((sum, item) => sum + item.quantity, 0));
  readonly subtotal = computed(() =>
    this.items().reduce(
      (sum, item) =>
        sum + calculateLineSubtotal(item.price, item.quantity, item.promoBuyQuantity, item.promoPayQuantity),
      0
    )
  );
  readonly promotionDiscounts = computed(() =>
    calculatePromotionDiscounts(buildCategoryCartLines(this.items()), this.promotions())
  );
  readonly promotionDiscount = computed(() => totalPromotionDiscount(this.promotionDiscounts()));
  readonly afterPromotions = computed(() => Math.max(0, this.subtotal() - this.promotionDiscount()));
  readonly couponCode = this.appliedCouponCode.asReadonly();
  readonly couponDiscount = this.appliedCouponAmount.asReadonly();
  readonly grandTotal = computed(() =>
    Math.max(0, this.afterPromotions() - this.appliedCouponAmount())
  );
  readonly hasMixedCurrency = computed(() => {
    const currencies = new Set(this.items().map((item) => item.currency));
    return currencies.size > 1 || (currencies.size === 1 && !currencies.has('UYU'));
  });

  constructor() {
    if (this.isBrowser) {
      this.contentApi.getPromotions().subscribe((promotions) => {
        this.promotions.set(promotions);
        this.restoreCoupon();
      });
    }
  }

  /** Coupon state is in-memory only (unlike cart items); re-validate a persisted code after a
   *  full page reload so it doesn't silently drop and let checkout proceed at full price. */
  private restoreCoupon(): void {
    const code = this.readCouponFromStorage();
    if (!code || this.items().length === 0) {
      return;
    }
    this.discountApi.validate({ code, eligibleSubtotal: this.afterPromotions() }).subscribe({
      next: (response) => this.setCoupon(response.code, response.discountAmount),
      error: () => this.clearCoupon(),
    });
  }

  addVariant(product: ProductDetail, variant: ProductVariantPublic, quantity = 1): void {
    const current = this.items();
    const existing = current.find((item) => item.variantId === variant.id);
    const maxQty = Math.min(Math.max(variant.stock, 1), MAX_LINE_QUANTITY);
    const displayName = this.displayName(product.name, variant);
    if (existing) {
      this.items.set(
        current.map((item) =>
          item.variantId === variant.id
            ? { ...item, quantity: Math.min(item.quantity + quantity, maxQty) }
            : item
        )
      );
    } else {
      const newItem: CartItem = {
        variantId: variant.id,
        productId: product.id,
        slug: product.slug,
        name: displayName,
        price: variant.price,
        currency: variant.currency,
        primaryImageUrl: variant.primaryImageUrl,
        storageCapacity: variant.storageCapacity,
        color: variant.color,
        condition: variant.condition,
        productType: product.productType,
        stock: variant.stock,
        quantity: Math.min(quantity, maxQty),
        promoBuyQuantity: product.promoBuyQuantity,
        promoPayQuantity: product.promoPayQuantity,
        categoryId: product.categoryId,
      };
      this.items.set([...current, newItem]);
    }
    this.persist();
  }

  updateQuantity(variantId: string, quantity: number): void {
    if (quantity <= 0) {
      this.removeItem(variantId);
      return;
    }
    this.items.set(
      this.items().map((item) => {
        if (item.variantId !== variantId) {
          return item;
        }
        const maxQty = Math.min(Math.max(item.stock, 1), MAX_LINE_QUANTITY);
        return { ...item, quantity: Math.min(quantity, maxQty) };
      })
    );
    this.persist();
  }

  removeItem(variantId: string): void {
    this.items.set(this.items().filter((item) => item.variantId !== variantId));
    this.persist();
  }

  clear(): void {
    this.items.set([]);
    this.clearCoupon();
    this.persist();
  }

  setCoupon(code: string, amount: number): void {
    this.appliedCouponCode.set(code.trim().toUpperCase());
    this.appliedCouponAmount.set(Math.max(0, amount));
    this.persistCoupon();
  }

  clearCoupon(): void {
    this.appliedCouponCode.set(null);
    this.appliedCouponAmount.set(0);
    this.persistCoupon();
  }

  private displayName(productName: string, variant: ProductVariantPublic): string {
    const parts = [productName];
    if (variant.color) {
      parts.push(variant.color);
    }
    if (variant.storageCapacity) {
      parts.push(variant.storageCapacity);
    }
    return parts.length === 1 ? productName : `${productName} — ${parts.slice(1).join(' / ')}`;
  }

  private persist(): void {
    if (!this.isBrowser) {
      return;
    }
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.items()));
    } catch {
      // localStorage unavailable
    }
  }

  private persistCoupon(): void {
    if (!this.isBrowser) {
      return;
    }
    try {
      const code = this.appliedCouponCode();
      if (code) {
        localStorage.setItem(COUPON_STORAGE_KEY, code);
      } else {
        localStorage.removeItem(COUPON_STORAGE_KEY);
      }
    } catch {
      // localStorage unavailable
    }
  }

  private readCouponFromStorage(): string | null {
    if (!this.isBrowser) {
      return null;
    }
    try {
      return localStorage.getItem(COUPON_STORAGE_KEY);
    } catch {
      return null;
    }
  }

  private readFromStorage(): CartItem[] {
    if (!this.isBrowser) {
      return [];
    }
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return [];
      }
      const parsed = JSON.parse(raw) as CartItem[];
      return Array.isArray(parsed)
        ? parsed.filter((item) => typeof item?.variantId === 'string')
        : [];
    } catch {
      return [];
    }
  }
}
