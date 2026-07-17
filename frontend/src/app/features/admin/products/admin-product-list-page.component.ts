import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AdminProductApiService } from '../../../core/api/admin-product-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import {
  AdminProductSummary,
  PageResponse,
} from '../../../shared/models/admin.models';
import { ProductCondition, ProductType } from '../../../shared/models/catalog.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { ConditionLabelPipe } from '../../../shared/pipes/condition-label.pipe';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';

interface ProductListRouteData {
  productType?: ProductType;
  title?: string;
  basePath?: string;
}

@Component({
  selector: 'app-admin-product-list-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    AdminBreadcrumbsComponent,
    StatePanelComponent,
    ConditionLabelPipe,
    MoneyPipe,
  ],
  templateUrl: './admin-product-list-page.component.html',
  styleUrl: './admin-product-list-page.component.scss',
})
export class AdminProductListPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly productApi = inject(AdminProductApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly pageTitle = signal('Productos');
  readonly basePath = signal('/admin/productos');
  readonly productType = signal<ProductType | undefined>('IPHONE');

  readonly state = signal<UiState<PageResponse<AdminProductSummary>>>(loadingState());
  readonly actionError = signal('');
  readonly page = signal(0);
  readonly pageSize = 20;

  readonly filters = this.fb.nonNullable.group({
    q: [''],
    condition: ['' as '' | ProductCondition],
    published: ['' as '' | 'true' | 'false'],
  });

  ngOnInit(): void {
    const data = this.route.snapshot.data as ProductListRouteData;
    if (data.title) {
      this.pageTitle.set(data.title);
    }
    if (data.basePath) {
      this.basePath.set(data.basePath);
    }
    if (data.productType) {
      this.productType.set(data.productType);
    }

    this.filters.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.page.set(0);
        this.load();
      });

    this.load();
  }

  load(): void {
    this.actionError.set('');
    this.state.set(loadingState());

    const { q, condition, published } = this.filters.getRawValue();
    this.productApi
      .search({
        q: q || undefined,
        productType: this.productType(),
        condition: condition || undefined,
        published: published === '' ? undefined : published === 'true',
        page: this.page(),
        size: this.pageSize,
        sort: 'updatedAt,desc',
      })
      .subscribe({
        next: (response) => {
          this.state.set(
            response.content.length === 0 && this.page() === 0
              ? { status: 'empty', message: 'No hay productos que coincidan con los filtros.' }
              : successState(response)
          );
        },
        error: (error) => {
          this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar los productos')));
        },
      });
  }

  goToPage(nextPage: number): void {
    const data = this.state().data;
    if (!data || nextPage < 0 || nextPage >= data.totalPages) {
      return;
    }
    this.page.set(nextPage);
    this.load();
  }

  togglePublish(product: AdminProductSummary): void {
    const action = product.published ? 'despublicar' : 'publicar';
    if (!confirmAction(`¿Confirmás ${action} "${product.name}"?`)) {
      return;
    }

    this.productApi.setPublished(product.id, !product.published).subscribe({
      next: () => this.load(),
      error: (error) => {
        this.actionError.set(apiErrorMessage(error, `No se pudo ${action} el producto`));
      },
    });
  }

  deleteProduct(product: AdminProductSummary): void {
    if (!confirmAction(`¿Eliminar "${product.name}"? Esta acción no se puede deshacer.`)) {
      return;
    }

    this.productApi.delete(product.id).subscribe({
      next: () => this.load(),
      error: (error) => {
        this.actionError.set(apiErrorMessage(error, 'No se pudo eliminar el producto'));
      },
    });
  }

  createLink(): string[] {
    return [this.basePath(), 'nuevo'];
  }

  editLink(id: string): string[] {
    return [this.basePath(), id];
  }
}
