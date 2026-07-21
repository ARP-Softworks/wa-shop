import { DatePipe, NgClass } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminOrderApiService } from '../../../core/api/admin-order-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { UiSelectComponent } from '../../../shared/components/ui-select/ui-select.component';
import { AdminOrderDetail, OrderStatus } from '../../../shared/models/admin.models';
import { ORDER_STATUS_OPTIONS } from '../../../shared/models/order-status-options';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { OrderStatusLabelPipe } from '../../../shared/pipes/order-status-label.pipe';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { scrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-admin-order-detail-page',
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
  templateUrl: './admin-order-detail-page.component.html',
  styleUrl: './admin-order-detail-page.component.scss',
})
export class AdminOrderDetailPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly orderApi = inject(AdminOrderApiService);

  readonly state = signal<UiState<AdminOrderDetail>>(loadingState());
  readonly submitError = signal('');
  readonly successMessage = signal('');
  readonly submitting = signal(false);
  readonly statusOptions = ORDER_STATUS_OPTIONS;

  readonly statusForm = this.fb.nonNullable.group({
    status: ['PENDING_PAYMENT' as OrderStatus, Validators.required],
    note: [''],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.load(id);
    }
  }

  load(id: string): void {
    this.state.set(loadingState());
    this.orderApi.getById(id).subscribe({
      next: (order) => {
        this.state.set(successState(order));
        this.statusForm.reset({ status: order.status, note: '' });
      },
      error: (error) => {
        this.state.set({ status: 'error', message: apiErrorMessage(error, 'No se pudo cargar el pedido') });
      },
    });
  }

  submitStatus(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.submitError.set('');
    this.successMessage.set('');
    this.statusForm.markAllAsTouched();

    if (this.statusForm.invalid) {
      return;
    }

    const raw = this.statusForm.getRawValue();
    this.submitting.set(true);

    this.orderApi.updateStatus(id, { status: raw.status, note: raw.note.trim() || null }).subscribe({
      next: (order) => {
        this.submitting.set(false);
        this.state.set(successState(order));
        this.statusForm.patchValue({ note: '' });
        this.successMessage.set('Estado actualizado correctamente.');
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo actualizar el estado'));
        if (parsed?.details) {
          this.submitError.set(Object.values(mapFieldErrors(parsed.details)).join(' '));
        }
        scrollToTop();
      },
    });
  }

  statusClass(status: OrderStatus): Record<string, boolean> {
    return { ['status-badge--' + status.toLowerCase().replace(/_/g, '-')]: true };
  }
}
