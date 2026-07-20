import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import {
  CartItem,
  ProductSummary,
  PublicPromotion,
  buildCategoryCartLines,
  calculateLineSubtotal,
  calculatePromotionDiscounts,
  totalPromotionDiscount,
} from '../../shared/models/catalog.models';
import { PublicContentApiService } from '../api/public-content-api.service';

const STORAGE_KEY = 'wa-shop-cart';
/** Keep in sync with backend OrderItemRequest @Max. */
const MAX_LINE_QUANTITY = 10;

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly contentApi = inject(PublicContentApiService);
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
      this.contentApi.getPromotions().subscribe((promotions) => this.promotions.set(promotions));
    }
  }

  addItem(product: ProductSummary, quantity = 1): void {
    const current = this.items();
    const existing = current.find((item) => item.productId === product.id);
    const maxQty = Math.min(Math.max(product.stock, 1), MAX_LINE_QUANTITY);
    if (existing) {
      this.items.set(
        current.map((item) =>
          item.productId === product.id
            ? { ...item, quantity: Math.min(item.quantity + quantity, maxQty) }
            : item
        )
      );
    } else {
      const newItem: CartItem = {
        productId: product.id,
        slug: product.slug,
        name: product.name,
        price: product.price,
        currency: product.currency,
        primaryImageUrl: product.primaryImageUrl,
        storageCapacity: product.storageCapacity,
        color: product.color,
        condition: product.condition,
        productType: product.productType,
        stock: product.stock,
        quantity: Math.min(quantity, maxQty),
        promoBuyQuantity: product.promoBuyQuantity,
        promoPayQuantity: product.promoPayQuantity,
        categoryId: product.categoryId,
      };
      this.items.set([...current, newItem]);
    }
    this.persist();
  }

  updateQuantity(productId: string, quantity: number): void {
    if (quantity <= 0) {
      this.removeItem(productId);
      return;
    }
    this.items.set(
      this.items().map((item) => {
        if (item.productId !== productId) {
          return item;
        }
        const maxQty = Math.min(Math.max(item.stock, 1), MAX_LINE_QUANTITY);
        return { ...item, quantity: Math.min(quantity, maxQty) };
      })
    );
    this.persist();
  }

  removeItem(productId: string): void {
    this.items.set(this.items().filter((item) => item.productId !== productId));
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
  }

  clearCoupon(): void {
    this.appliedCouponCode.set(null);
    this.appliedCouponAmount.set(0);
  }

  private persist(): void {
    if (!this.isBrowser) {
      return;
    }
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.items()));
    } catch {
      // localStorage unavailable (e.g. private browsing) — cart just won't persist across reloads.
    }
  }

  private readFromStorage(): CartItem[] {
    if (!this.isBrowser) {
      return [];
    }
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as CartItem[]) : [];
    } catch {
      return [];
    }
  }
}
