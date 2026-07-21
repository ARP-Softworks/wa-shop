import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminHeroBannerApiService } from '../../../core/api/admin-hero-banner-api.service';
import { CanComponentDeactivate } from '../../../core/auth/can-deactivate.guard';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { AdminMediaUploaderComponent } from '../../../shared/components/admin-media-uploader/admin-media-uploader.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminHeroBanner, MediaUploadResponse } from '../../../shared/models/admin.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { scrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-admin-hero-banner-form-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminBreadcrumbsComponent, StatePanelComponent, AdminMediaUploaderComponent],
  templateUrl: './admin-hero-banner-form-page.component.html',
  styleUrl: './admin-hero-banner-form-page.component.scss',
})
export class AdminHeroBannerFormPageComponent implements OnInit, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly bannerApi = inject(AdminHeroBannerApiService);

  readonly isEdit = signal(false);
  readonly bannerId = signal<string | null>(null);
  readonly loadState = signal<UiState<AdminHeroBanner>>(loadingState());
  readonly submitError = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly successMessage = signal('');
  readonly submitting = signal(false);
  readonly imageUploadError = signal('');

  readonly form = this.fb.nonNullable.group({
    imageUrl: ['', Validators.required],
    imagePublicId: [''],
    altText: ['', Validators.maxLength(255)],
    linkUrl: ['', Validators.maxLength(500)],
    position: [0, Validators.required],
    active: [true],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nuevo') {
      this.isEdit.set(true);
      this.bannerId.set(id);
      this.loadBanner(id);
    } else {
      this.loadState.set(successState({} as AdminHeroBanner));
    }
  }

  canDeactivate(): boolean {
    if (!this.form.dirty || this.submitting()) {
      return true;
    }
    return confirmAction('Hay cambios sin guardar. ¿Salir de todos modos?');
  }

  onImageUploaded(response: MediaUploadResponse): void {
    this.imageUploadError.set('');
    this.form.patchValue({ imageUrl: response.url, imagePublicId: response.publicId });
    this.form.markAsDirty();
  }

  onImageUploadFailed(message: string): void {
    this.imageUploadError.set(message);
  }

  clearImage(): void {
    this.form.patchValue({ imageUrl: '', imagePublicId: '' });
    this.form.markAsDirty();
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

    const raw = this.form.getRawValue();
    const payload = {
      imageUrl: raw.imageUrl.trim(),
      imagePublicId: raw.imagePublicId.trim() || null,
      altText: raw.altText.trim() || null,
      linkUrl: raw.linkUrl.trim() || null,
      position: Math.trunc(Number(raw.position)),
      active: raw.active,
    };

    this.submitting.set(true);
    const request$ = this.isEdit()
      ? this.bannerApi.update(this.bannerId()!, payload)
      : this.bannerApi.create(payload);

    request$.subscribe({
      next: (banner) => {
        this.submitting.set(false);
        this.form.markAsPristine();
        this.successMessage.set('Banner guardado correctamente.');
        if (!this.isEdit()) {
          void this.router.navigate(['/admin/banners', banner.id]);
        }
        scrollToTop();
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo guardar el banner'));
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

  private loadBanner(id: string): void {
    this.loadState.set(loadingState());
    this.bannerApi.getById(id).subscribe({
      next: (banner) => {
        this.loadState.set(successState(banner));
        this.form.reset({
          imageUrl: banner.imageUrl,
          imagePublicId: banner.imagePublicId ?? '',
          altText: banner.altText ?? '',
          linkUrl: banner.linkUrl ?? '',
          position: banner.position,
          active: banner.active,
        });
        this.form.markAsPristine();
      },
      error: (error) => {
        this.loadState.set({ status: 'error', message: apiErrorMessage(error, 'No se pudo cargar el banner') });
      },
    });
  }
}
