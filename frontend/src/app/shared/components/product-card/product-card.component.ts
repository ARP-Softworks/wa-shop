import { Component, Input, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductSummary } from '../../models/catalog.models';
import { MoneyPipe } from '../../pipes/money.pipe';
import { ConditionLabelPipe } from '../../pipes/condition-label.pipe';
import { productAlt } from '../../utils/product-alt.util';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { AnalyticsService } from '../../../core/analytics/analytics.service';

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
}
