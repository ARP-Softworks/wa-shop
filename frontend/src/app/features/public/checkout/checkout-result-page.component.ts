import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderApiService } from '../../../core/api/order-api.service';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { OrderStatus } from '../../../shared/models/catalog.models';

type ResultKind = 'exito' | 'error' | 'pendiente';

const MAX_POLL_ATTEMPTS = 4;
const POLL_DELAY_MS = 2000;

@Component({
  selector: 'app-checkout-result-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './checkout-result-page.component.html',
  styleUrl: './checkout-result-page.component.scss',
})
export class CheckoutResultPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly orderApi = inject(OrderApiService);
  private readonly whatsapp = inject(WhatsappLinkService);

  kind: ResultKind = 'pendiente';
  status: OrderStatus | null = null;
  checking = true;
  hasOrderId = false;
  private orderId: string | null = null;

  ngOnInit(): void {
    this.kind = (this.route.snapshot.data['kind'] as ResultKind) ?? 'pendiente';
    const orderId =
      this.route.snapshot.queryParamMap.get('orderId') ||
      this.route.snapshot.queryParamMap.get('order_id');
    if (!orderId) {
      this.checking = false;
      return;
    }
    this.orderId = orderId;
    this.hasOrderId = true;
    this.pollStatus(orderId, 0);
  }

  get whatsappUrl(): string | null {
    return this.orderId ? this.whatsapp.buildOrderInquiryUrl(this.orderId) : this.whatsapp.buildGeneralInquiryUrl();
  }

  get title(): string {
    if (this.status === 'PAID' || this.status === 'CONFIRMED') {
      return '¡Pago confirmado!';
    }
    if (this.status === 'REJECTED') {
      return 'El pago fue rechazado';
    }
    if (this.status === 'CANCELLED' || this.status === 'EXPIRED') {
      return 'El pago fue cancelado';
    }
    if (this.kind === 'error') {
      return 'Algo no salió bien con el pago';
    }
    return 'Estamos confirmando tu pago';
  }

  get description(): string {
    if (this.status === 'PAID' || this.status === 'CONFIRMED') {
      return 'Para coordinar la entrega, escribinos por WhatsApp confirmando tu número de pedido.';
    }
    if (this.status === 'REJECTED' || this.status === 'CANCELLED' || this.status === 'EXPIRED') {
      return 'Podés volver a intentarlo desde el catálogo, o consultarnos por WhatsApp.';
    }
    if (this.checking) {
      return 'Esto puede tardar unos segundos…';
    }
    return 'Todavía no tenemos confirmación de Mercado Pago. Si ya pagaste, contactanos por WhatsApp con el número de pedido.';
  }

  private pollStatus(orderId: string, attempt: number): void {
    this.orderApi.getStatus(orderId).subscribe({
      next: (response) => {
        this.status = response.status;
        if (response.status === 'PENDING_PAYMENT' && attempt < MAX_POLL_ATTEMPTS) {
          setTimeout(() => this.pollStatus(orderId, attempt + 1), POLL_DELAY_MS);
        } else {
          this.checking = false;
        }
      },
      error: () => {
        this.checking = false;
      },
    });
  }
}
