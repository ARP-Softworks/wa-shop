import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CartService } from '../../../core/cart/cart.service';
import { PublicDiscountCodeApiService } from '../../../core/api/public-discount-code-api.service';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { CartItem, calculateLineSubtotal } from '../../../shared/models/catalog.models';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [RouterLink, MoneyPipe, FormsModule],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.scss',
})
export class CartPageComponent {
  readonly cart = inject(CartService);
  private readonly discountApi = inject(PublicDiscountCodeApiService);

  couponInput = '';
  readonly couponError = signal('');
  readonly couponBusy = signal(false);

  increment(variantId: string, current: number): void {
    this.cart.updateQuantity(variantId, current + 1);
  }

  decrement(variantId: string, current: number): void {
    this.cart.updateQuantity(variantId, current - 1);
  }

  remove(variantId: string): void {
    this.cart.removeItem(variantId);
  }

  lineSubtotal(item: CartItem): number {
    return calculateLineSubtotal(item.price, item.quantity, item.promoBuyQuantity, item.promoPayQuantity);
  }

  promoLabel(item: CartItem): string | null {
    if (!item.promoBuyQuantity || !item.promoPayQuantity) {
      return null;
    }
    return `${item.promoBuyQuantity}x${item.promoPayQuantity}`;
  }

  lineDiscount(item: CartItem): number {
    return this.cart.promotionDiscounts().get(item.variantId) ?? 0;
  }

  applyCoupon(): void {
    this.couponError.set('');
    const code = this.couponInput.trim();
    if (!code) {
      this.couponError.set('Ingresá un código');
      return;
    }
    this.couponBusy.set(true);
    this.discountApi
      .validate({ code, eligibleSubtotal: this.cart.afterPromotions() })
      .subscribe({
        next: (response) => {
          this.couponBusy.set(false);
          this.cart.setCoupon(response.code, response.discountAmount);
          this.couponInput = response.code;
        },
        error: (error) => {
          this.couponBusy.set(false);
          this.cart.clearCoupon();
          this.couponError.set(apiErrorMessage(error, 'Código no válido'));
        },
      });
  }

  removeCoupon(): void {
    this.cart.clearCoupon();
    this.couponInput = '';
    this.couponError.set('');
  }
}
