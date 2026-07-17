import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { CartItem, ProductSummary } from '../../shared/models/catalog.models';

const STORAGE_KEY = 'wa-shop-cart';

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly items = signal<CartItem[]>(this.readFromStorage());

  readonly cartItems = this.items.asReadonly();
  readonly itemCount = computed(() => this.items().reduce((sum, item) => sum + item.quantity, 0));
  readonly subtotal = computed(() =>
    this.items().reduce((sum, item) => sum + item.price * item.quantity, 0)
  );
  readonly hasMixedCurrency = computed(() => {
    const currencies = new Set(this.items().map((item) => item.currency));
    return currencies.size > 1 || (currencies.size === 1 && !currencies.has('UYU'));
  });

  addItem(product: ProductSummary, quantity = 1): void {
    const current = this.items();
    const existing = current.find((item) => item.productId === product.id);
    if (existing) {
      this.items.set(
        current.map((item) =>
          item.productId === product.id
            ? { ...item, quantity: Math.min(item.quantity + quantity, Math.max(item.stock, 1)) }
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
        quantity: Math.min(quantity, Math.max(product.stock, 1)),
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
      this.items().map((item) =>
        item.productId === productId
          ? { ...item, quantity: Math.min(quantity, Math.max(item.stock, 1)) }
          : item
      )
    );
    this.persist();
  }

  removeItem(productId: string): void {
    this.items.set(this.items().filter((item) => item.productId !== productId));
    this.persist();
  }

  clear(): void {
    this.items.set([]);
    this.persist();
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
