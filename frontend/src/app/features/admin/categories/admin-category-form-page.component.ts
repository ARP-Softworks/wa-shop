import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AdminCategoryApiService } from '../../../core/api/admin-category-api.service';
import { CanComponentDeactivate } from '../../../core/auth/can-deactivate.guard';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminCategory } from '../../../shared/models/admin.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { slugify } from '../../../shared/utils/slugify.util';

@Component({
  selector: 'app-admin-category-form-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminBreadcrumbsComponent, StatePanelComponent],
  templateUrl: './admin-category-form-page.component.html',
  styleUrl: './admin-category-form-page.component.scss',
})
export class AdminCategoryFormPageComponent implements OnInit, OnDestroy, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly categoryApi = inject(AdminCategoryApiService);

  readonly isEdit = signal(false);
  readonly categoryId = signal<string | null>(null);
  readonly loadState = signal<UiState<AdminCategory>>(loadingState());
  readonly submitError = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly successMessage = signal('');
  readonly submitting = signal(false);

  private slugManuallyEdited = false;
  private subscriptions = new Subscription();

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    slug: ['', [Validators.required, Validators.maxLength(140)]],
    description: [''],
    active: [true],
    seoTitle: ['', Validators.maxLength(70)],
    metaDescription: ['', Validators.maxLength(320)],
    indexable: [true],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nuevo') {
      this.isEdit.set(true);
      this.categoryId.set(id);
      this.loadCategory(id);
    } else {
      this.loadState.set(successState({} as AdminCategory));
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

    const body = this.form.getRawValue();
    const payload = {
      name: body.name.trim(),
      slug: body.slug.trim(),
      description: body.description.trim() || null,
      active: body.active,
      seoTitle: body.seoTitle.trim() || null,
      metaDescription: body.metaDescription.trim() || null,
      indexable: body.indexable,
    };

    this.submitting.set(true);
    const request$ = this.isEdit()
      ? this.categoryApi.update(this.categoryId()!, payload)
      : this.categoryApi.create(payload);

    request$.subscribe({
      next: (category) => {
        this.submitting.set(false);
        this.form.markAsPristine();
        this.successMessage.set('Categoría guardada correctamente.');
        if (!this.isEdit()) {
          void this.router.navigate(['/admin/categorias', category.id]);
        }
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo guardar la categoría'));
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

  private loadCategory(id: string): void {
    this.loadState.set(loadingState());
    this.categoryApi.getById(id).subscribe({
      next: (category) => {
        this.loadState.set(successState(category));
        this.form.reset({
          name: category.name,
          slug: category.slug,
          description: category.description ?? '',
          active: category.active,
          seoTitle: category.seoTitle ?? '',
          metaDescription: category.metaDescription ?? '',
          indexable: category.indexable ?? true,
        });
        this.form.markAsPristine();
        this.slugManuallyEdited = true;
      },
      error: (error) => {
        this.loadState.set({
          status: 'error',
          message: apiErrorMessage(error, 'No se pudo cargar la categoría'),
        });
      },
    });
  }
}
