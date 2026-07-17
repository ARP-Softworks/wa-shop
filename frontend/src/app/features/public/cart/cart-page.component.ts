import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CartService } from '../../../core/cart/cart.service';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';

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
}
