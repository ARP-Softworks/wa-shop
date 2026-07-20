import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminCategoryApiService } from '../../../core/api/admin-category-api.service';
import { AdminPromotionApiService } from '../../../core/api/admin-promotion-api.service';
import { CanComponentDeactivate } from '../../../core/auth/can-deactivate.guard';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { UiSelectComponent } from '../../../shared/components/ui-select/ui-select.component';
import { AdminCategory, AdminPromotion } from '../../../shared/models/admin.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';

@Component({
  selector: 'app-admin-promotion-form-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminBreadcrumbsComponent, StatePanelComponent, UiSelectComponent],
  templateUrl: './admin-promotion-form-page.component.html',
  styleUrl: './admin-promotion-form-page.component.scss',
})
export class AdminPromotionFormPageComponent implements OnInit, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly promotionApi = inject(AdminPromotionApiService);
  private readonly categoryApi = inject(AdminCategoryApiService);

  readonly isEdit = signal(false);
  readonly promotionId = signal<string | null>(null);
  readonly loadState = signal<UiState<AdminPromotion>>(loadingState());
  readonly categories = signal<AdminCategory[]>([]);
  readonly submitError = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly successMessage = signal('');
  readonly submitting = signal(false);

  readonly categoryOptions = computed(() => this.categories().map((c) => ({ value: c.id, label: c.name })));

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    active: [true],
    triggerCategoryId: ['', Validators.required],
    triggerQuantity: [2, [Validators.required, Validators.min(1)]],
    rewardCategoryId: ['', Validators.required],
    rewardQuantity: [1, [Validators.required, Validators.min(1)]],
    discountPercent: [100, [Validators.required, Validators.min(1), Validators.max(100)]],
  });

  ngOnInit(): void {
    this.categoryApi.list().subscribe({
      next: (categories) => this.categories.set(categories),
      error: () => this.categories.set([]),
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nueva') {
      this.isEdit.set(true);
      this.promotionId.set(id);
      this.loadPromotion(id);
    } else {
      this.loadState.set(successState({} as AdminPromotion));
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
    const payload = {
      name: body.name.trim(),
      active: body.active,
      triggerCategoryId: body.triggerCategoryId,
      triggerQuantity: Math.trunc(Number(body.triggerQuantity)),
      rewardCategoryId: body.rewardCategoryId,
      rewardQuantity: Math.trunc(Number(body.rewardQuantity)),
      discountPercent: Math.trunc(Number(body.discountPercent)),
    };

    this.submitting.set(true);
    const request$ = this.isEdit()
      ? this.promotionApi.update(this.promotionId()!, payload)
      : this.promotionApi.create(payload);

    request$.subscribe({
      next: (promotion) => {
        this.submitting.set(false);
        this.form.markAsPristine();
        this.successMessage.set('Promoción guardada correctamente.');
        if (!this.isEdit()) {
          void this.router.navigate(['/admin/promociones', promotion.id]);
        }
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo guardar la promoción'));
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

  private loadPromotion(id: string): void {
    this.loadState.set(loadingState());
    this.promotionApi.getById(id).subscribe({
      next: (promotion) => {
        this.loadState.set(successState(promotion));
        this.form.reset({
          name: promotion.name,
          active: promotion.active,
          triggerCategoryId: promotion.triggerCategoryId,
          triggerQuantity: promotion.triggerQuantity,
          rewardCategoryId: promotion.rewardCategoryId,
          rewardQuantity: promotion.rewardQuantity,
          discountPercent: promotion.discountPercent,
        });
        this.form.markAsPristine();
      },
      error: (error) => {
        this.loadState.set({ status: 'error', message: apiErrorMessage(error, 'No se pudo cargar la promoción') });
      },
    });
  }
}
