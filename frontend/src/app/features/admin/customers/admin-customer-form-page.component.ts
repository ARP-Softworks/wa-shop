import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminCustomerApiService } from '../../../core/api/admin-customer-api.service';
import { AdminOrderApiService } from '../../../core/api/admin-order-api.service';
import { CanComponentDeactivate } from '../../../core/auth/can-deactivate.guard';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminCustomer, AdminOrderSummary } from '../../../shared/models/admin.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { OrderStatusLabelPipe } from '../../../shared/pipes/order-status-label.pipe';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { scrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-admin-customer-form-page',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    AdminBreadcrumbsComponent,
    StatePanelComponent,
    MoneyPipe,
    OrderStatusLabelPipe,
  ],
  templateUrl: './admin-customer-form-page.component.html',
  styleUrl: './admin-customer-form-page.component.scss',
})
export class AdminCustomerFormPageComponent implements OnInit, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly customerApi = inject(AdminCustomerApiService);
  private readonly orderApi = inject(AdminOrderApiService);

  readonly isEdit = signal(false);
  readonly customerId = signal<string | null>(null);
  readonly loadState = signal<UiState<AdminCustomer>>(loadingState());
  readonly orders = signal<AdminOrderSummary[]>([]);
  readonly submitError = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly successMessage = signal('');
  readonly submitting = signal(false);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    phone: ['', [Validators.required, Validators.maxLength(40)]],
    email: ['', [Validators.email]],
    address: [''],
    notes: [''],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nuevo') {
      this.isEdit.set(true);
      this.customerId.set(id);
      this.loadCustomer(id);
    } else {
      this.loadState.set(successState({} as AdminCustomer));
    }
  }

  canDeactivate(): boolean {
    if (!this.form.dirty || this.submitting()) {
      return true;
    }
    return confirmAction('Hay cambios sin guardar. ¿Salir de todos modos?');
  }

  submit(): void {
    this.submitError.set('');
    this.fieldErrors.set({});
    this.successMessage.set('');
    this.form.markAllAsTouched();

    if (this.form.invalid) {
      this.submitError.set('Revisá los campos marcados.');
      scrollToTop();
      return;
    }

    const body = this.form.getRawValue();
    const payload = {
      name: body.name.trim(),
      phone: body.phone.trim(),
      email: body.email.trim() || null,
      address: body.address.trim() || null,
      notes: body.notes.trim() || null,
    };

    this.submitting.set(true);
    const request$ = this.isEdit()
      ? this.customerApi.update(this.customerId()!, payload)
      : this.customerApi.create(payload);

    request$.subscribe({
      next: (customer) => {
        this.submitting.set(false);
        this.form.markAsPristine();
        this.successMessage.set('Cliente guardado correctamente.');
        if (!this.isEdit()) {
          void this.router.navigate(['/admin/clientes', customer.id]);
        }
        scrollToTop();
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo guardar el cliente'));
        if (parsed?.details) {
          this.fieldErrors.set(mapFieldErrors(parsed.details));
        }
        scrollToTop();
      },
    });
  }

  fieldError(field: string): string | null {
    const backend = this.fieldErrors()[field];
    if (backend) {
      return backend;
    }
    const control = this.form.get(field);
    if (!control?.touched || !control.invalid) {
      return null;
    }
    return control.hasError('required') ? 'Campo obligatorio' : 'Valor inválido';
  }

  private loadCustomer(id: string): void {
    this.loadState.set(loadingState());
    this.customerApi.getById(id).subscribe({
      next: (customer) => {
        this.loadState.set(successState(customer));
        this.form.reset({
          name: customer.name,
          phone: customer.phone,
          email: customer.email ?? '',
          address: customer.address ?? '',
          notes: customer.notes ?? '',
        });
        this.form.markAsPristine();
      },
      error: (error) => {
        this.loadState.set({ status: 'error', message: apiErrorMessage(error, 'No se pudo cargar el cliente') });
      },
    });
    this.orderApi.search({ customerId: id, size: 10 }).subscribe({
      next: (response) => this.orders.set(response.content),
      error: () => this.orders.set([]),
    });
  }
}
