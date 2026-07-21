import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogApiService } from '../../../core/api/catalog-api.service';
import { PublicContentApiService } from '../../../core/api/public-content-api.service';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { ProductSummary, PublicHeroBanner } from '../../../shared/models/catalog.models';
import { UiState, emptyState, errorState, loadingState, successState } from '../../../shared/models/ui-state';

const ROTATE_INTERVAL_MS = 6000;

// Fixed in code (not admin/DB-driven) so the hero always looks the same in every environment —
// the DB-backed banner upload was inconsistent across dev/production (missing rows, opaque
// backgrounds). Swap these two files to change the hero images.
const FIXED_HERO_BANNERS: PublicHeroBanner[] = [
  {
    id: 'fixed-iphones',
    imageUrl: 'assets/marketing/hero-banner-iphones.png',
    altText: 'Línea de iPhone en todos los colores disponibles',
    linkUrl: '/iphone',
  },
  {
    id: 'fixed-accessories',
    imageUrl: 'assets/marketing/hero-banner-accessories.png',
    altText: 'Accesorios originales: cargador MagSafe y AirPods',
    linkUrl: '/accesorios',
  },
];

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

  readonly featuredState = signal<UiState<ProductSummary[]>>(loadingState());
  readonly banners = signal<PublicHeroBanner[]>(FIXED_HERO_BANNERS);
  readonly activeBanner = signal(0);

  private rotateTimer: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.catalogApi.search({ featured: true, size: 5 }).subscribe({
      next: (page) => {
        if (page.content.length === 0) {
          this.featuredState.set(emptyState('Todavía no hay productos destacados publicados.'));
        } else {
          this.featuredState.set(successState(page.content));
        }
      },
      error: () => this.featuredState.set(errorState('No se pudieron cargar los destacados.')),
    });

    if (this.banners().length > 1) {
      this.startRotation();
    }
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

  get businessName(): string {
    return this.contentApi.settings()?.businessName || 'WA Shop';
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
