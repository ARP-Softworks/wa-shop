import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../../core/cart/cart.service';
import { OrderApiService } from '../../../core/api/order-api.service';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-checkout-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MoneyPipe],
  templateUrl: './checkout-page.component.html',
  styleUrl: './checkout-page.component.scss',
})
export class CheckoutPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly orderApi = inject(OrderApiService);
  private readonly router = inject(Router);
  readonly cart = inject(CartService);

  readonly form = this.fb.nonNullable.group({
    customerName: ['', [Validators.required, Validators.maxLength(200)]],
    customerPhone: ['', [Validators.required, Validators.maxLength(40)]],
    customerEmail: ['', [Validators.email]],
    shippingAddress: ['', [Validators.maxLength(2000)]],
  });

  submitting = false;
  errorMessage = '';

  ngOnInit(): void {
    if (this.cart.cartItems().length === 0) {
      void this.router.navigate(['/carrito']);
    }
  }

  fieldError(field: string): string | null {
    const control = this.form.get(field);
    if (!control?.touched || !control.invalid) {
      return null;
    }
    if (control.hasError('required')) {
      return 'Campo obligatorio';
    }
    if (control.hasError('email')) {
      return 'Email inválido';
    }
    if (control.hasError('maxlength')) {
      return 'Es demasiado largo';
    }
    return 'Valor inválido';
  }

  submit(): void {
    this.errorMessage = '';
    if (this.form.invalid || this.cart.cartItems().length === 0 || this.cart.hasMixedCurrency()) {
      this.form.markAllAsTouched();
      if (this.form.invalid) {
        this.errorMessage = 'Revisá los campos marcados.';
      }
      return;
    }

    this.submitting = true;
    const value = this.form.getRawValue();
    this.orderApi
      .create({
        customerName: value.customerName,
        customerPhone: value.customerPhone,
        customerEmail: value.customerEmail || null,
        shippingAddress: value.shippingAddress || null,
        items: this.cart.cartItems().map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      })
      .subscribe({
        next: (response) => {
          this.cart.clear();
          window.location.href = response.checkoutUrl;
        },
        error: (error: unknown) => {
          this.submitting = false;
          this.errorMessage = apiErrorMessage(error, 'No se pudo iniciar el pago. Intentá de nuevo.');
        },
      });
  }
}
