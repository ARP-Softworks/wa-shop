import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogApiService } from '../../../core/api/catalog-api.service';
import { PublicContentApiService } from '../../../core/api/public-content-api.service';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { AnalyticsService } from '../../../core/analytics/analytics.service';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { ProductSummary, PublicHeroBanner } from '../../../shared/models/catalog.models';
import { UiState, emptyState, errorState, loadingState, successState } from '../../../shared/models/ui-state';

const ROTATE_INTERVAL_MS = 6000;

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [RouterLink, ProductCardComponent],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss',
})
export class HomePageComponent implements OnInit, OnDestroy {
  private readonly catalogApi = inject(CatalogApiService);
  readonly contentApi = inject(PublicContentApiService);
  private readonly whatsapp = inject(WhatsappLinkService);
  private readonly analytics = inject(AnalyticsService);

  readonly featuredState = signal<UiState<ProductSummary[]>>(loadingState());
  readonly banners = signal<PublicHeroBanner[]>([]);
  readonly activeBanner = signal(0);

  heroImageSrc = 'assets/marketing/hero-iphones-pedestal.png';
  heroImageOk = true;

  private rotateTimer: ReturnType<typeof setInterval> | null = null;

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

    this.contentApi.getHeroBanners().subscribe((banners) => {
      this.banners.set(banners);
      if (banners.length > 1) {
        this.startRotation();
      }
    });
  }

  ngOnDestroy(): void {
    this.stopRotation();
  }

  isInternalLink(url: string): boolean {
    return url.startsWith('/');
  }

  goToBanner(index: number): void {
    this.activeBanner.set(index);
  }

  private startRotation(): void {
    this.stopRotation();
    this.rotateTimer = setInterval(() => {
      const total = this.banners().length;
      this.activeBanner.set((this.activeBanner() + 1) % total);
    }, ROTATE_INTERVAL_MS);
  }

  private stopRotation(): void {
    if (this.rotateTimer !== null) {
      clearInterval(this.rotateTimer);
      this.rotateTimer = null;
    }
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
