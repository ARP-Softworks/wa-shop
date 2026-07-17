import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogApiService } from '../../../core/api/catalog-api.service';
import { PublicContentApiService } from '../../../core/api/public-content-api.service';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { AnalyticsService } from '../../../core/analytics/analytics.service';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { ProductSummary } from '../../../shared/models/catalog.models';
import { UiState, emptyState, errorState, loadingState, successState } from '../../../shared/models/ui-state';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [RouterLink, ProductCardComponent, StatePanelComponent],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss',
})
export class HomePageComponent implements OnInit {
  private readonly catalogApi = inject(CatalogApiService);
  readonly contentApi = inject(PublicContentApiService);
  private readonly whatsapp = inject(WhatsappLinkService);
  private readonly analytics = inject(AnalyticsService);

  readonly featuredState = signal<UiState<ProductSummary[]>>(loadingState());

  heroImageSrc = 'assets/marketing/hero-iphones.png';
  heroImageOk = true;

  ngOnInit(): void {
    this.catalogApi.search({ featured: true, productType: 'IPHONE', size: 5 }).subscribe({
      next: (page) => {
        if (page.content.length === 0) {
          this.featuredState.set(emptyState('Todavía no hay productos destacados publicados.'));
        } else {
          this.featuredState.set(successState(page.content));
        }
      },
      error: () => this.featuredState.set(errorState('No se pudieron cargar los destacados.')),
    });
  }

  get whatsappUrl(): string | null {
    return this.whatsapp.buildGeneralInquiryUrl();
  }

  get businessName(): string {
    return this.contentApi.settings()?.businessName || 'WA Shop';
  }

  onWhatsappClick(): void {
    this.analytics.trackWhatsappClick('home');
  }

  onHeroImageError(): void {
    this.heroImageOk = false;
  }

  hideBrokenImage(event: Event): void {
    const img = event.target as HTMLImageElement | null;
    if (!img) {
      return;
    }
    const media = img.parentElement;
    img.remove();
    media?.classList.add('category-card__media--fallback');
  }
}
