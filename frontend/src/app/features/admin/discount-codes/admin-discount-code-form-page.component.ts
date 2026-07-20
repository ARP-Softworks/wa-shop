import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminDiscountCodeApiService } from '../../../core/api/admin-discount-code-api.service';
import { CanComponentDeactivate } from '../../../core/auth/can-deactivate.guard';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { UiSelectComponent } from '../../../shared/components/ui-select/ui-select.component';
import { AdminDiscountCode, DiscountType } from '../../../shared/models/admin.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';

@Component({
  selector: 'app-admin-discount-code-form-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminBreadcrumbsComponent, StatePanelComponent, UiSelectComponent],
  templateUrl: './admin-discount-code-form-page.component.html',
  styleUrl: './admin-discount-code-form-page.component.scss',
})
export class AdminDiscountCodeFormPageComponent implements OnInit, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(AdminDiscountCodeApiService);

  readonly isEdit = signal(false);
  readonly codeId = signal<string | null>(null);
  readonly loadState = signal<UiState<AdminDiscountCode>>(loadingState());
  readonly usedCount = signal(0);
  readonly submitError = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly successMessage = signal('');
  readonly submitting = signal(false);

  readonly discountTypeOptions = [
    { value: 'PERCENT', label: 'Porcentaje' },
    { value: 'FIXED', label: 'Monto fijo (UYU)' },
  ];

  readonly form = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.maxLength(40)]],
    active: [true],
    discountType: ['PERCENT' as DiscountType, Validators.required],
    discountValue: [10, [Validators.required, Validators.min(0.01)]],
    maxUses: [null as number | null],
    startsAt: [''],
    endsAt: [''],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nuevo') {
      this.isEdit.set(true);
      this.codeId.set(id);
      this.loadCode(id);
    } else {
      this.loadState.set(successState({} as AdminDiscountCode));
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
      return;
    }

    const body = this.form.getRawValue();
    const maxUsesRaw = body.maxUses;
    const payload = {
      code: body.code.trim().toUpperCase(),
      active: body.active,
      discountType: body.discountType,
      discountValue: Number(body.discountValue),
      maxUses: maxUsesRaw == null || maxUsesRaw === ('' as unknown) ? null : Math.trunc(Number(maxUsesRaw)),
      startsAt: this.toIsoOrNull(body.startsAt),
      endsAt: this.toIsoOrNull(body.endsAt),
    };

    if (payload.discountType === 'PERCENT' && payload.discountValue > 100) {
      this.submitError.set('El porcentaje no puede superar 100.');
      return;
    }

    this.submitting.set(true);
    const request$ = this.isEdit()
      ? this.api.update(this.codeId()!, payload)
      : this.api.create(payload);

    request$.subscribe({
      next: (saved) => {
        this.submitting.set(false);
        this.form.markAsPristine();
        this.successMessage.set('Código guardado correctamente.');
        this.usedCount.set(saved.usedCount);
        if (!this.isEdit()) {
          void this.router.navigate(['/admin/codigos-descuento', saved.id]);
        }
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo guardar el código'));
        if (parsed?.details) {
          this.fieldErrors.set(mapFieldErrors(parsed.details));
        }
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
    if (control.hasError('required')) {
      return 'Campo obligatorio';
    }
    if (control.hasError('min') || control.hasError('max')) {
      return 'Valor fuera de rango';
    }
    return 'Valor inválido';
  }

  private loadCode(id: string): void {
    this.loadState.set(loadingState());
    this.api.getById(id).subscribe({
      next: (code) => {
        this.loadState.set(successState(code));
        this.usedCount.set(code.usedCount);
        this.form.reset({
          code: code.code,
          active: code.active,
          discountType: code.discountType,
          discountValue: code.discountValue,
          maxUses: code.maxUses,
          startsAt: this.toDatetimeLocal(code.startsAt),
          endsAt: this.toDatetimeLocal(code.endsAt),
        });
        this.form.markAsPristine();
      },
      error: (error) => {
        this.loadState.set({ status: 'error', message: apiErrorMessage(error, 'No se pudo cargar el código') });
      },
    });
  }

  private toDatetimeLocal(iso: string | null): string {
    if (!iso) {
      return '';
    }
    const date = new Date(iso);
    if (Number.isNaN(date.getTime())) {
      return '';
    }
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  private toIsoOrNull(value: string): string | null {
    if (!value?.trim()) {
      return null;
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date.toISOString();
  }
}
