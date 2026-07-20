import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { combineLatest, debounceTime, distinctUntilChanged } from 'rxjs';
import { CatalogApiService } from '../../../core/api/catalog-api.service';
import { PublicContentApiService } from '../../../core/api/public-content-api.service';
import { AnalyticsService } from '../../../core/analytics/analytics.service';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../../shared/components/breadcrumbs/breadcrumbs.component';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { UiSelectComponent } from '../../../shared/components/ui-select/ui-select.component';
import {
  ProductCondition,
  ProductSearchParams,
  ProductSummary,
  ProductType,
} from '../../../shared/models/catalog.models';
import { modelFromSlug } from '../../../shared/utils/product-alt.util';
import { UiState, emptyState, errorState, loadingState, successState } from '../../../shared/models/ui-state';

@Component({
  selector: 'app-catalog-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ProductCardComponent,
    StatePanelComponent,
    BreadcrumbsComponent,
    UiSelectComponent,
  ],
  templateUrl: './catalog-page.component.html',
  styleUrl: './catalog-page.component.scss',
})
export class CatalogPageComponent implements OnInit {
  private readonly catalogApi = inject(CatalogApiService);
  private readonly contentApi = inject(PublicContentApiService);
  private readonly analytics = inject(AnalyticsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly productType = signal<ProductType>('IPHONE');
  readonly title = signal('Catálogo');
  readonly intro = signal('');
  readonly showBatteryFilter = signal(true);
  readonly conditionLocked = signal(false);
  readonly modelLocked = signal(false);
  readonly listState = signal<UiState<ProductSummary[]>>(loadingState());
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly crumbs = signal<BreadcrumbItem[]>([]);
  readonly categoryLabel = signal<string | null>(null);

  readonly conditionOptions = [
    { value: '', label: 'Todos' },
    { value: 'NEW', label: 'Nuevo' },
    { value: 'USED', label: 'Usado' },
  ];

  readonly sortOptions = [
    { value: 'createdAt,desc', label: 'Más recientes' },
    { value: 'createdAt,asc', label: 'Más antiguos' },
    { value: 'price,asc', label: 'Precio: menor a mayor' },
    { value: 'price,desc', label: 'Precio: mayor a menor' },
  ];

  readonly filters = this.fb.nonNullable.group({
    q: [''],
    condition: [''],
    model: [''],
    storageCapacity: [''],
    color: [''],
    minBatteryHealth: [''],
    minPrice: [''],
    maxPrice: [''],
    sort: ['createdAt,desc'],
  });

  ngOnInit(): void {
    combineLatest([this.route.data, this.route.paramMap, this.route.queryParamMap])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(([data, params, query]) => {
        const productType = (data['productType'] as ProductType) || 'IPHONE';
        this.productType.set(productType);
        this.showBatteryFilter.set(productType === 'IPHONE');

        const presetCondition = (data['condition'] as ProductCondition | undefined) || null;
        this.conditionLocked.set(!!presetCondition);

        const modelSlug = params.get('modelSlug');
        const modelPreset = modelSlug ? modelFromSlug(modelSlug) : '';
        this.modelLocked.set(!!modelSlug);

        const categorySlug = params.get('categorySlug');
        let pageTitle = (data['title'] as string) || 'Catálogo';
        let intro = (data['intro'] as string) || '';

        if (modelPreset) {
          pageTitle = `iPhone ${modelPreset}`;
          intro = `Equipos iPhone modelo ${modelPreset} publicados en WA Shop Uruguay.`;
        }

        if (categorySlug) {
          this.contentApi.getCategories().subscribe((categories) => {
            const category = categories.find((c) => c.slug === categorySlug);
            if (category) {
              this.categoryLabel.set(category.name);
              this.title.set(category.name);
              this.intro.set(category.description || intro);
              this.crumbs.set(this.buildCrumbs(productType, category.name, presetCondition, modelPreset));
            }
          });
        } else {
          this.categoryLabel.set(null);
        }

        this.title.set(pageTitle);
        this.intro.set(intro);
        this.crumbs.set(this.buildCrumbs(productType, null, presetCondition, modelPreset));

        this.filters.patchValue(
          {
            q: query.get('q') ?? '',
            condition: presetCondition || query.get('condition') || '',
            model: modelPreset || query.get('model') || '',
            storageCapacity: query.get('storageCapacity') ?? '',
            color: query.get('color') ?? '',
            minBatteryHealth: query.get('minBatteryHealth') ?? '',
            minPrice: query.get('minPrice') ?? '',
            maxPrice: query.get('maxPrice') ?? '',
            sort: query.get('sort') ?? 'createdAt,desc',
          },
          { emitEvent: false }
        );
        this.page.set(Number(query.get('page') ?? 0));
        this.loadProducts();
      });

    this.filters.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        if (value.q?.trim()) {
          this.analytics.trackSearch(value.q.trim());
        }
        this.page.set(0);
        this.syncUrl();
      });
  }

  clearFilters(): void {
    const condition = this.conditionLocked() ? this.filters.controls.condition.value : '';
    const model = this.modelLocked() ? this.filters.controls.model.value : '';
    this.filters.reset({
      q: '',
      condition,
      model,
      storageCapacity: '',
      color: '',
      minBatteryHealth: '',
      minPrice: '',
      maxPrice: '',
      sort: 'createdAt,desc',
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) {
      return;
    }
    this.page.set(page);
    this.syncUrl();
  }

  pageLink(page: number): Record<string, string | number> {
    const value = this.filters.getRawValue();
    const queryParams: Record<string, string | number> = {};
    Object.entries({ ...value, page }).forEach(([key, val]) => {
      if (this.conditionLocked() && key === 'condition') {
        return;
      }
      if (this.modelLocked() && key === 'model') {
        return;
      }
      if (val !== '' && val !== null && val !== undefined && !(key === 'page' && val === 0)) {
        queryParams[key] = val as string | number;
      }
    });
    return queryParams;
  }

  private buildCrumbs(
    productType: ProductType,
    categoryName: string | null,
    condition: ProductCondition | null,
    model: string
  ): BreadcrumbItem[] {
    const items: BreadcrumbItem[] = [{ label: 'Inicio', link: '/' }];
    if (productType === 'ACCESSORY') {
      items.push({ label: 'Accesorios', link: '/accesorios' });
      if (categoryName) {
        items.push({ label: categoryName });
      }
      return items;
    }
    items.push({ label: 'iPhone', link: '/iphone' });
    if (condition === 'NEW') {
      items.push({ label: 'Nuevos' });
    } else if (condition === 'USED') {
      items.push({ label: 'Usados' });
    } else if (model) {
      items.push({ label: model });
    }
    return items;
  }

  private buildSearchParams(): ProductSearchParams {
    const value = this.filters.getRawValue();
    return {
      productType: this.productType(),
      q: value.q || undefined,
      condition: (value.condition as ProductCondition) || undefined,
      model: value.model || undefined,
      storageCapacity: value.storageCapacity || undefined,
      color: value.color || undefined,
      minBatteryHealth: value.minBatteryHealth ? Number(value.minBatteryHealth) : undefined,
      minPrice: value.minPrice ? Number(value.minPrice) : undefined,
      maxPrice: value.maxPrice ? Number(value.maxPrice) : undefined,
      sort: value.sort || undefined,
      page: this.page(),
      size: 12,
    };
  }

  private syncUrl(): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: this.pageLink(this.page()),
      replaceUrl: true,
    });
  }

  private loadProducts(): void {
    this.listState.set(loadingState());
    this.catalogApi.search(this.buildSearchParams()).subscribe({
      next: (response) => {
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        if (response.content.length === 0) {
          this.listState.set(emptyState('No encontramos productos con esos filtros.'));
        } else {
          this.listState.set(successState(response.content));
        }
      },
      error: () => this.listState.set(errorState('No se pudo cargar el catálogo.')),
    });
  }
}
