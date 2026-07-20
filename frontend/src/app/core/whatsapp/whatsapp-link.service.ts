import { Injectable } from '@angular/core';
import { ProductCondition, ProductSummary } from '../../shared/models/catalog.models';
import { PublicContentApiService } from '../api/public-content-api.service';

@Injectable({ providedIn: 'root' })
export class WhatsappLinkService {
  constructor(private readonly contentApi: PublicContentApiService) {}

  buildProductInquiryUrl(
    product: Pick<ProductSummary, 'name'>,
    variant: Pick<
      ProductSummary,
      'condition' | 'storageCapacity' | 'color' | 'batteryHealth' | 'currency' | 'price'
    >
  ): string | null {
    const phone = this.normalizePhone(this.contentApi.settings()?.whatsappNumber);
    if (!phone) {
      return null;
    }

    const parts: string[] = [`Hola, estoy interesado en el ${product.name}`];

    parts.push(this.conditionLabel(variant.condition));

    if (variant.storageCapacity) {
      parts.push(variant.storageCapacity);
    }
    if (variant.color) {
      parts.push(`color ${variant.color}`);
    }
    if (variant.batteryHealth != null) {
      parts.push(`batería ${variant.batteryHealth}%`);
    }

    parts.push(`publicado a ${variant.currency} ${this.formatPrice(variant.price)}`);

    const message = `${parts.join(', ')}. ¿Sigue disponible?`;
    return `https://wa.me/${phone}?text=${encodeURIComponent(message)}`;
  }

  buildGeneralInquiryUrl(message = 'Hola, quisiera consultar por productos de WA Shop.'): string | null {
    const phone = this.normalizePhone(this.contentApi.settings()?.whatsappNumber);
    if (!phone) {
      return null;
    }
    return `https://wa.me/${phone}?text=${encodeURIComponent(message)}`;
  }

  /** Prompts the customer to reach out and confirm delivery after paying — mirrors OrderEmailService. */
  buildOrderInquiryUrl(orderId: string): string | null {
    const phone = this.normalizePhone(this.contentApi.settings()?.whatsappNumber);
    if (!phone) {
      return null;
    }
    const businessName = this.contentApi.settings()?.businessName || 'WA Shop';
    const orderCode = `#${orderId.slice(0, 8).toUpperCase()}`;
    const message = `Hola! Realicé el pedido ${orderCode} en ${businessName} y quiero coordinar la entrega.`;
    return `https://wa.me/${phone}?text=${encodeURIComponent(message)}`;
  }

  private conditionLabel(condition: ProductCondition): string {
    return condition === 'NEW' ? 'nuevo' : 'usado';
  }

  private formatPrice(price: number): string {
    return new Intl.NumberFormat('es-UY', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(price);
  }

  private normalizePhone(phone: string | null | undefined): string | null {
    if (!phone) {
      return null;
    }
    const digits = phone.replace(/\D/g, '');
    return digits.length > 0 ? digits : null;
  }
}
