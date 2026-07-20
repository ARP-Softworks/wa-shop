import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CartService } from '../../../core/cart/cart.service';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { CartItem, calculateLineSubtotal } from '../../../shared/models/catalog.models';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [RouterLink, MoneyPipe],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.scss',
})
export class CartPageComponent {
  readonly cart = inject(CartService);

  increment(productId: string, current: number): void {
    this.cart.updateQuantity(productId, current + 1);
  }

  decrement(productId: string, current: number): void {
    this.cart.updateQuantity(productId, current - 1);
  }

  remove(productId: string): void {
    this.cart.removeItem(productId);
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
    return this.cart.promotionDiscounts().get(item.productId) ?? 0;
  }
}
