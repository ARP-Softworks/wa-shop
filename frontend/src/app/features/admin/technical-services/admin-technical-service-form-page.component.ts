import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AdminTechnicalServiceApiService } from '../../../core/api/admin-technical-service-api.service';
import { CanComponentDeactivate } from '../../../core/auth/can-deactivate.guard';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminTechnicalService } from '../../../shared/models/admin.models';
import { CurrencyCode } from '../../../shared/models/catalog.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { slugify } from '../../../shared/utils/slugify.util';

@Component({
  selector: 'app-admin-technical-service-form-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminBreadcrumbsComponent, StatePanelComponent],
  templateUrl: './admin-technical-service-form-page.component.html',
  styleUrl: './admin-technical-service-form-page.component.scss',
})
export class AdminTechnicalServiceFormPageComponent implements OnInit, OnDestroy, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly serviceApi = inject(AdminTechnicalServiceApiService);

  readonly isEdit = signal(false);
  readonly serviceId = signal<string | null>(null);
  readonly loadState = signal<UiState<AdminTechnicalService>>(loadingState());
  readonly submitError = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly successMessage = signal('');
  readonly submitting = signal(false);

  private slugManuallyEdited = false;
  private subscriptions = new Subscription();

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    slug: ['', [Validators.required, Validators.maxLength(220)]],
    description: [''],
    price: this.fb.control<number | null>(null, Validators.min(0)),
    currency: ['UYU' as CurrencyCode],
    estimatedTime: ['', Validators.maxLength(120)],
    active: [true],
    seoTitle: ['', Validators.maxLength(70)],
    metaDescription: ['', Validators.maxLength(320)],
    indexable: [true],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nuevo') {
      this.isEdit.set(true);
      this.serviceId.set(id);
      this.loadService(id);
    } else {
      this.loadState.set(successState({} as AdminTechnicalService));
    }

    this.subscriptions.add(
      this.form.controls.name.valueChanges.subscribe((name) => {
        if (!this.slugManuallyEdited) {
          this.form.controls.slug.setValue(slugify(name), { emitEvent: false });
        }
      })
    );

    this.subscriptions.add(
      this.form.controls.slug.valueChanges.subscribe(() => {
        this.slugManuallyEdited = true;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
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

    const raw = this.form.getRawValue();
    const payload = {
      name: raw.name.trim(),
      slug: raw.slug.trim(),
      description: raw.description.trim() || null,
      price: raw.price,
      currency: raw.currency,
      estimatedTime: raw.estimatedTime.trim() || null,
      active: raw.active,
      seoTitle: raw.seoTitle.trim() || null,
      metaDescription: raw.metaDescription.trim() || null,
      indexable: raw.indexable,
    };

    this.submitting.set(true);
    const request$ = this.isEdit()
      ? this.serviceApi.update(this.serviceId()!, payload)
      : this.serviceApi.create(payload);

    request$.subscribe({
      next: (service) => {
        this.submitting.set(false);
        this.form.markAsPristine();
        this.successMessage.set('Servicio guardado correctamente.');
        if (!this.isEdit()) {
          void this.router.navigate(['/admin/servicios', service.id]);
        }
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo guardar el servicio'));
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
    return control.hasError('required') ? 'Campo obligatorio' : 'Valor inválido';
  }

  private loadService(id: string): void {
    this.loadState.set(loadingState());
    this.serviceApi.getById(id).subscribe({
      next: (service) => {
        this.loadState.set(successState(service));
        this.form.reset({
          name: service.name,
          slug: service.slug,
          description: service.description ?? '',
          price: service.price,
          currency: service.currency ?? 'UYU',
          estimatedTime: service.estimatedTime ?? '',
          active: service.active,
          seoTitle: service.seoTitle ?? '',
          metaDescription: service.metaDescription ?? '',
          indexable: service.indexable ?? true,
        });
        this.form.markAsPristine();
        this.slugManuallyEdited = true;
      },
      error: (error) => {
        this.loadState.set({
          status: 'error',
          message: apiErrorMessage(error, 'No se pudo cargar el servicio'),
        });
      },
    });
  }
}
