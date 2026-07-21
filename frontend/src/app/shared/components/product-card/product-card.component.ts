import { Component, Input, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductSummary } from '../../models/catalog.models';
import { MoneyPipe } from '../../pipes/money.pipe';
import { ConditionLabelPipe } from '../../pipes/condition-label.pipe';
import { productAlt } from '../../utils/product-alt.util';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { AnalyticsService } from '../../../core/analytics/analytics.service';
import { CatalogApiService } from '../../../core/api/catalog-api.service';
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
  private readonly catalogApi = inject(CatalogApiService);
  private readonly cart = inject(CartService);

  readonly adding = signal(false);
  readonly added = signal(false);
  readonly addError = signal<string | null>(null);

  get promoLabel(): string | null {
    const { promoBuyQuantity, promoPayQuantity } = this.product;
    if (!promoBuyQuantity || !promoPayQuantity) {
      return null;
    }
    return `${promoBuyQuantity}x${promoPayQuantity}`;
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
    return this.whatsapp.buildProductInquiryUrl(this.product, this.product);
  }

  onWhatsappClick(event: Event): void {
    event.stopPropagation();
    this.analytics.trackWhatsappClick('product_card');
  }

  quickAdd(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    if (this.adding() || this.product.stock <= 0) {
      return;
    }
    this.adding.set(true);
    this.addError.set(null);
    // The card only carries the parent product's denormalized price/stock; the actual
    // purchasable unit is a ProductVariant, so fetch the detail and use its cheapest
    // in-stock variant (variants[0], server-sorted) — same default the detail page applies.
    this.catalogApi.getBySlug(this.product.slug).subscribe({
      next: (detail) => {
        this.adding.set(false);
        const variant = detail.variants[0];
        if (!variant || variant.stock <= 0 || variant.currency !== 'UYU') {
          this.addError.set('No disponible para agregar al carrito.');
          return;
        }
        this.cart.addVariant(detail, variant);
        this.analytics.trackAddToCart('product_card');
        this.added.set(true);
        setTimeout(() => this.added.set(false), 1500);
      },
      error: () => {
        this.adding.set(false);
        this.addError.set('No se pudo agregar. Intentá de nuevo.');
      },
    });
  }
}
