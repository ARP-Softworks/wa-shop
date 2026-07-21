import { Component, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CatalogApiService } from '../../../core/api/catalog-api.service';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { AnalyticsService } from '../../../core/analytics/analytics.service';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../../shared/components/breadcrumbs/breadcrumbs.component';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { ConditionLabelPipe } from '../../../shared/pipes/condition-label.pipe';
import {
  ProductDetail,
  ProductImage,
  ProductSummary,
  ProductVariantPublic,
} from '../../../shared/models/catalog.models';
import { productAlt } from '../../../shared/utils/product-alt.util';
import { UiState, errorState, loadingState, successState } from '../../../shared/models/ui-state';
import { CartService } from '../../../core/cart/cart.service';

@Component({
  selector: 'app-product-detail-page',
  standalone: true,
  imports: [
    RouterLink,
    ProductCardComponent,
    StatePanelComponent,
    MoneyPipe,
    ConditionLabelPipe,
    BreadcrumbsComponent,
  ],
  templateUrl: './product-detail-page.component.html',
  styleUrl: './product-detail-page.component.scss',
})
export class ProductDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly catalogApi = inject(CatalogApiService);
  private readonly whatsapp = inject(WhatsappLinkService);
  private readonly analytics = inject(AnalyticsService);
  private readonly cart = inject(CartService);

  readonly detailState = signal<UiState<ProductDetail>>(loadingState());
  readonly related = signal<ProductSummary[]>([]);
  readonly selectedVariantId = signal<string | null>(null);
  readonly activeImage = signal<ProductImage | null>(null);
  readonly lightboxOpen = signal(false);
  added = false;

  readonly selectedVariant = computed(() => {
    const product = this.detailState().data;
    if (!product?.variants?.length) {
      return null;
    }
    const id = this.selectedVariantId();
    return product.variants.find((v) => v.id === id) ?? product.variants[0];
  });

  get variantColors(): string[] {
    const product = this.detailState().data;
    if (!product) {
      return [];
    }
    return Array.from(
      new Set(product.variants.map((v) => v.color).filter((c): c is string => !!c))
    );
  }

  capacitiesForColor(color: string): string[] {
    const product = this.detailState().data;
    if (!product) {
      return [];
    }
    return Array.from(
      new Set(
        product.variants
          .filter((v) => v.color === color)
          .map((v) => v.storageCapacity)
          .filter((c): c is string => !!c)
      )
    );
  }

  isCurrentColor(color: string): boolean {
    return this.selectedVariant()?.color === color;
  }

  isCurrentCapacity(capacity: string): boolean {
    return this.selectedVariant()?.storageCapacity === capacity;
  }

  selectColor(color: string): void {
    const product = this.detailState().data;
    if (!product) {
      return;
    }
    const currentCapacity = this.selectedVariant()?.storageCapacity;
    const match =
      product.variants.find((v) => v.color === color && v.storageCapacity === currentCapacity) ??
      product.variants.find((v) => v.color === color);
    if (match) {
      this.applyVariant(match);
    }
  }

  selectCapacity(capacity: string): void {
    const product = this.detailState().data;
    if (!product) {
      return;
    }
    const currentColor = this.selectedVariant()?.color;
    const match = product.variants.find(
      (v) => v.color === currentColor && v.storageCapacity === capacity
    );
    if (match) {
      this.applyVariant(match);
    }
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const slug = params.get('slug');
      if (!slug) {
        this.detailState.set(errorState('Producto no encontrado.'));
        return;
      }
      this.load(slug);
    });
  }

  get crumbs(): BreadcrumbItem[] {
    const product = this.detailState().data;
    if (!product) {
      return [{ label: 'Inicio', link: '/' }];
    }
    if (product.productType === 'ACCESSORY') {
      return [
        { label: 'Inicio', link: '/' },
        { label: 'Accesorios', link: '/accesorios' },
        { label: product.name },
      ];
    }
    return [
      { label: 'Inicio', link: '/' },
      { label: 'iPhone', link: '/iphone' },
      { label: product.name },
    ];
  }

  get catalogLink(): string {
    return this.detailState().data?.productType === 'ACCESSORY' ? '/accesorios' : '/iphone';
  }

  get whatsappUrl(): string | null {
    const product = this.detailState().data;
    const variant = this.selectedVariant();
    return product && variant ? this.whatsapp.buildProductInquiryUrl(product, variant) : null;
  }

  get mainAlt(): string {
    const product = this.detailState().data;
    if (!product) {
      return '';
    }
    return this.activeImage()?.altText || productAlt(product);
  }

  get stockMessage(): string {
    const stock = this.selectedVariant()?.stock ?? 0;
    if (stock <= 0) {
      return 'Sin stock publicado — consultá disponibilidad por WhatsApp.';
    }
    if (stock === 1) {
      return '1 unidad disponible.';
    }
    return `${stock} unidades disponibles.`;
  }

  selectImage(image: ProductImage): void {
    this.activeImage.set(image);
  }

  openLightbox(): void {
    if (this.activeImage() || this.selectedVariant()?.primaryImageUrl) {
      this.lightboxOpen.set(true);
    }
  }

  closeLightbox(): void {
    this.lightboxOpen.set(false);
  }

  shiftLightboxImage(step: number): void {
    const variant = this.selectedVariant();
    const images = variant?.images ?? [];
    if (images.length < 2) {
      return;
    }
    const currentId = this.activeImage()?.id;
    const currentIndex = Math.max(
      images.findIndex((img) => img.id === currentId),
      0
    );
    const nextIndex = (currentIndex + step + images.length) % images.length;
    this.activeImage.set(images[nextIndex]);
  }

  @HostListener('document:keydown', ['$event'])
  onLightboxKeydown(event: KeyboardEvent): void {
    if (!this.lightboxOpen()) {
      return;
    }
    if (event.key === 'Escape') {
      this.closeLightbox();
    } else if (event.key === 'ArrowRight') {
      this.shiftLightboxImage(1);
    } else if (event.key === 'ArrowLeft') {
      this.shiftLightboxImage(-1);
    }
  }

  onWhatsappClick(): void {
    this.analytics.trackWhatsappClick('product-detail');
  }

  get canAddToCart(): boolean {
    const variant = this.selectedVariant();
    return !!variant && variant.currency === 'UYU' && variant.stock > 0;
  }

  addToCart(): void {
    const product = this.detailState().data;
    const variant = this.selectedVariant();
    if (!product || !variant) {
      return;
    }
    this.cart.addVariant(product, variant);
    this.added = true;
    setTimeout(() => (this.added = false), 1500);
  }

  productAltFallback(product: ProductDetail): string {
    return productAlt(product);
  }

  private applyVariant(variant: ProductVariantPublic): void {
    this.selectedVariantId.set(variant.id);
    const main =
      variant.images.find((img) => img.mainImage) ||
      variant.images[0] ||
      null;
    this.activeImage.set(main);
  }

  private load(slug: string): void {
    this.detailState.set(loadingState());
    this.selectedVariantId.set(null);
    this.catalogApi.getBySlug(slug).subscribe({
      next: (product) => {
        this.detailState.set(successState(product));
        const initial = product.variants[0] ?? null;
        if (initial) {
          this.applyVariant(initial);
        } else {
          this.activeImage.set(null);
        }
        this.analytics.trackProductView(product.id, product.slug);
        this.catalogApi.related(slug).subscribe((items) => this.related.set(items));
      },
      error: () => this.detailState.set(errorState('No se pudo cargar el producto.')),
    });
  }
}
