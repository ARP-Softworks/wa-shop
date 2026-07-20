import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CatalogApiService } from '../../../core/api/catalog-api.service';
import { WhatsappLinkService } from '../../../core/whatsapp/whatsapp-link.service';
import { AnalyticsService } from '../../../core/analytics/analytics.service';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../../shared/components/breadcrumbs/breadcrumbs.component';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { ConditionLabelPipe } from '../../../shared/pipes/condition-label.pipe';
import { ProductDetail, ProductImage, ProductSummary } from '../../../shared/models/catalog.models';
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
  private readonly router = inject(Router);
  private readonly catalogApi = inject(CatalogApiService);
  private readonly whatsapp = inject(WhatsappLinkService);
  private readonly analytics = inject(AnalyticsService);
  private readonly cart = inject(CartService);

  readonly detailState = signal<UiState<ProductDetail>>(loadingState());
  readonly related = signal<ProductSummary[]>([]);
  readonly variants = signal<ProductSummary[]>([]);
  readonly activeImage = signal<ProductImage | null>(null);
  added = false;

  get variantColors(): string[] {
    return Array.from(
      new Set(this.variants().map((v) => v.color).filter((c): c is string => !!c))
    );
  }

  capacitiesForColor(color: string): string[] {
    return Array.from(
      new Set(
        this.variants()
          .filter((v) => v.color === color)
          .map((v) => v.storageCapacity)
          .filter((c): c is string => !!c)
      )
    );
  }

  isCurrentColor(color: string): boolean {
    return this.detailState().data?.color === color;
  }

  isCurrentCapacity(capacity: string): boolean {
    return this.detailState().data?.storageCapacity === capacity;
  }

  selectColor(color: string): void {
    const currentCapacity = this.detailState().data?.storageCapacity;
    const match =
      this.variants().find((v) => v.color === color && v.storageCapacity === currentCapacity) ??
      this.variants().find((v) => v.color === color);
    if (match) {
      this.navigateToVariant(match);
    }
  }

  selectCapacity(capacity: string): void {
    const currentColor = this.detailState().data?.color;
    const match = this.variants().find(
      (v) => v.color === currentColor && v.storageCapacity === capacity
    );
    if (match) {
      this.navigateToVariant(match);
    }
  }

  private navigateToVariant(variant: ProductSummary): void {
    const base = variant.productType === 'ACCESSORY' ? '/accesorios' : '/iphone';
    void this.router.navigate([base, variant.slug]);
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
    return product ? this.whatsapp.buildProductInquiryUrl(product) : null;
  }

  get mainAlt(): string {
    const product = this.detailState().data;
    if (!product) {
      return '';
    }
    return this.activeImage()?.altText || productAlt(product);
  }

  get stockMessage(): string {
    const stock = this.detailState().data?.stock ?? 0;
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

  onWhatsappClick(): void {
    this.analytics.trackWhatsappClick('product-detail');
  }

  get canAddToCart(): boolean {
    const product = this.detailState().data;
    return !!product && product.currency === 'UYU' && product.stock > 0;
  }

  addToCart(): void {
    const product = this.detailState().data;
    if (!product) {
      return;
    }
    this.cart.addItem(product);
    this.added = true;
    setTimeout(() => (this.added = false), 1500);
  }

  productAltFallback(product: ProductDetail): string {
    return productAlt(product);
  }

  private load(slug: string): void {
    this.detailState.set(loadingState());
    this.variants.set([]);
    this.catalogApi.getBySlug(slug).subscribe({
      next: (product) => {
        this.detailState.set(successState(product));
        const main =
          product.images.find((img) => img.mainImage) ||
          product.images[0] ||
          null;
        this.activeImage.set(main);
        this.analytics.trackProductView(product.id, product.slug);
        this.catalogApi.related(slug).subscribe((items) => this.related.set(items));
        if (product.productGroupId) {
          this.catalogApi.variants(slug).subscribe((items) => this.variants.set(items));
        }
      },
      error: () => this.detailState.set(errorState('No se pudo cargar el producto.')),
    });
  }
}
