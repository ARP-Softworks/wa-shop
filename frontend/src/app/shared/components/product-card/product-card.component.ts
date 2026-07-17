import { Component, Input, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductSummary } from '../../models/catalog.models';
import { MoneyPipe } from '../../pipes/money.pipe';
import { ConditionLabelPipe } from '../../pipes/condition-label.pipe';
import { productAlt } from '../../utils/product-alt.util';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { AnalyticsService } from '../../../core/analytics/analytics.service';
import { CartService } from '../../../core/cart/cart.service';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [RouterLink, MoneyPipe, ConditionLabelPipe],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss',
})
export class ProductCardComponent {
  @Input({ required: true }) product!: ProductSummary;
  /** Compact layout for featured home grid (mockup density). */
  @Input() compact = false;

  private readonly whatsapp = inject(WhatsappLinkService);
  private readonly analytics = inject(AnalyticsService);
  private readonly cart = inject(CartService);

  added = false;

  get canAddToCart(): boolean {
    return this.product.currency === 'UYU' && this.product.stock > 0;
  }

  addToCart(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.cart.addItem(this.product);
    this.added = true;
    setTimeout(() => (this.added = false), 1500);
  }

  get detailLink(): string {
    return this.product.productType === 'ACCESSORY'
      ? `/accesorios/${this.product.slug}`
      : `/iphone/${this.product.slug}`;
  }

  get imageAlt(): string {
    return productAlt(this.product);
  }

  get whatsappUrl(): string | null {
    return this.whatsapp.buildProductInquiryUrl(this.product);
  }

  onWhatsappClick(event: Event): void {
    event.stopPropagation();
    this.analytics.trackWhatsappClick('product_card');
  }
}
