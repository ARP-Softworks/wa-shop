import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PublicContentApiService } from '../../../core/api/public-content-api.service';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { AnalyticsService } from '../../../core/analytics/analytics.service';
import { BreadcrumbsComponent } from '../../../shared/components/breadcrumbs/breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { TechnicalServiceItem } from '../../../shared/models/catalog.models';
import { UiState, errorState, loadingState, successState } from '../../../shared/models/ui-state';

@Component({
  selector: 'app-technical-service-detail-page',
  standalone: true,
  imports: [RouterLink, BreadcrumbsComponent, StatePanelComponent, MoneyPipe],
  templateUrl: './technical-service-detail-page.component.html',
  styleUrl: './technical-service-detail-page.component.scss',
})
export class TechnicalServiceDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly contentApi = inject(PublicContentApiService);
  private readonly whatsapp = inject(WhatsappLinkService);
  private readonly analytics = inject(AnalyticsService);

  readonly detailState = signal<UiState<TechnicalServiceItem>>(loadingState());

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const slug = params.get('slug');
      if (!slug) {
        this.detailState.set(errorState('Servicio no encontrado.'));
        return;
      }
      this.load(slug);
    });
  }

  get crumbs() {
    const service = this.detailState().data;
    return [
      { label: 'Inicio', link: '/' as string },
      { label: 'Servicio técnico', link: '/servicio-tecnico' },
      { label: service?.name || 'Detalle' },
    ];
  }

  get whatsappUrl(): string | null {
    const service = this.detailState().data;
    if (!service) {
      return null;
    }
    return this.whatsapp.buildGeneralInquiryUrl(
      `Hola, estoy interesado en el servicio técnico: ${service.name}. ¿Me pueden asesorar?`
    );
  }

  onWhatsappClick(): void {
    this.analytics.trackWhatsappClick('technical-service-detail');
  }

  private load(slug: string): void {
    this.detailState.set(loadingState());
    this.contentApi.getTechnicalServiceBySlug(slug).subscribe({
      next: (service) => this.detailState.set(successState(service)),
      error: () => this.detailState.set(errorState('No se pudo cargar el servicio.')),
    });
  }
}
