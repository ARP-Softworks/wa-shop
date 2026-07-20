import { DatePipe, NgClass } from '@angular/common';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { AdminOrderApiService } from '../../../core/api/admin-order-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { UiSelectComponent } from '../../../shared/components/ui-select/ui-select.component';
import { AdminOrderSummary, OrderStatus, PageResponse } from '../../../shared/models/admin.models';
import { ORDER_STATUS_FILTER_OPTIONS } from '../../../shared/models/order-status-options';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { OrderStatusLabelPipe } from '../../../shared/pipes/order-status-label.pipe';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-admin-order-list-page',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    ReactiveFormsModule,
    RouterLink,
    AdminBreadcrumbsComponent,
    StatePanelComponent,
    MoneyPipe,
    OrderStatusLabelPipe,
    UiSelectComponent,
  ],
  templateUrl: './admin-order-list-page.component.html',
  styleUrl: './admin-order-list-page.component.scss',
})
export class AdminOrderListPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly orderApi = inject(AdminOrderApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly state = signal<UiState<PageResponse<AdminOrderSummary>>>(loadingState());
  readonly page = signal(0);
  readonly pageSize = 20;
  readonly statusOptions = ORDER_STATUS_FILTER_OPTIONS;

  readonly filters = this.fb.nonNullable.group({
    status: ['' as '' | OrderStatus],
  });

  ngOnInit(): void {
    this.filters.controls.status.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.page.set(0);
        this.load();
      });
    this.load();
  }

  load(): void {
    this.state.set(loadingState());
    const status = this.filters.controls.status.value;
    this.orderApi
      .search({ status: status || undefined, page: this.page(), size: this.pageSize })
      .subscribe({
        next: (response) => {
          this.state.set(
            response.content.length === 0 && this.page() === 0
              ? { status: 'empty', message: 'No hay pedidos con ese filtro.' }
              : successState(response)
          );
        },
        error: (error) => {
          this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar los pedidos')));
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

  statusClass(status: OrderStatus): Record<string, boolean> {
    return { ['status-badge--' + status.toLowerCase().replace(/_/g, '-')]: true };
  }
}
