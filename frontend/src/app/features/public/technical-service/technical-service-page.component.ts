import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PublicContentApiService } from '../../../core/api/public-content-api.service';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { BreadcrumbsComponent } from '../../../shared/components/breadcrumbs/breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { TechnicalServiceItem } from '../../../shared/models/catalog.models';
import { UiState, emptyState, errorState, loadingState, successState } from '../../../shared/models/ui-state';

@Component({
  selector: 'app-technical-service-page',
  standalone: true,
  imports: [RouterLink, StatePanelComponent, MoneyPipe, BreadcrumbsComponent],
  templateUrl: './technical-service-page.component.html',
  styleUrl: './technical-service-page.component.scss',
})
export class TechnicalServicePageComponent implements OnInit {
  private readonly contentApi = inject(PublicContentApiService);
  private readonly whatsapp = inject(WhatsappLinkService);

  readonly listState = signal<UiState<TechnicalServiceItem[]>>(loadingState());
  readonly crumbs = [
    { label: 'Inicio', link: '/' },
    { label: 'Servicio técnico' },
  ];

  ngOnInit(): void {
    this.contentApi.getTechnicalServices().subscribe({
      next: (items) => {
        if (items.length === 0) {
          this.listState.set(emptyState('No hay servicios técnicos publicados por ahora.'));
        } else {
          this.listState.set(successState(items));
        }
      },
      error: () => this.listState.set(errorState('No se pudieron cargar los servicios.')),
    });
  }

  whatsappFor(service: TechnicalServiceItem): string | null {
    return this.whatsapp.buildGeneralInquiryUrl(
      `Hola, estoy interesado en el servicio técnico: ${service.name}. ¿Me pueden asesorar?`
    );
  }
}
