import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminSettingsApiService } from '../../../core/api/admin-settings-api.service';
import { CanComponentDeactivate } from '../../../core/auth/can-deactivate.guard';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { AdminMediaUploaderComponent } from '../../../shared/components/admin-media-uploader/admin-media-uploader.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { MediaUploadResponse, SiteSettings } from '../../../shared/models/admin.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { scrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-admin-settings-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    AdminBreadcrumbsComponent,
    StatePanelComponent,
    AdminMediaUploaderComponent,
  ],
  templateUrl: './admin-settings-page.component.html',
  styleUrl: './admin-settings-page.component.scss',
})
export class AdminSettingsPageComponent implements OnInit, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly settingsApi = inject(AdminSettingsApiService);

  readonly state = signal<UiState<SiteSettings>>(loadingState());
  readonly submitError = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly successMessage = signal('');
  readonly submitting = signal(false);
  readonly logoUploadError = signal('');

  readonly form = this.fb.nonNullable.group({
    businessName: ['', [Validators.required, Validators.maxLength(200)]],
    whatsappNumber: ['', Validators.maxLength(40)],
    instagramUrl: ['', Validators.maxLength(500)],
    email: ['', [Validators.email, Validators.maxLength(320)]],
    address: ['', Validators.maxLength(500)],
    hours: ['', Validators.maxLength(500)],
    logoUrl: [''],
    logoPublicId: [''],
    publicSiteUrl: ['', Validators.maxLength(500)],
    defaultSeoTitle: ['', Validators.maxLength(70)],
    defaultMetaDescription: ['', Validators.maxLength(320)],
    defaultSocialImageUrl: [''],
    country: ['', Validators.maxLength(80)],
    region: ['', Validators.maxLength(120)],
    city: ['', Validators.maxLength(120)],
    postalCode: ['', Validators.maxLength(40)],
    googleSiteVerification: ['', Validators.maxLength(120)],
    bingSiteVerification: ['', Validators.maxLength(120)],
  });

  ngOnInit(): void {
    this.load();
  }

  canDeactivate(): boolean {
    if (!this.form.dirty || this.submitting()) {
      return true;
    }
    return confirmAction('Hay cambios sin guardar. ¿Salir de todos modos?');
  }

  load(): void {
    this.state.set(loadingState());
    this.settingsApi.get().subscribe({
      next: (settings) => {
        this.state.set(successState(settings));
        this.form.reset({
          businessName: settings.businessName,
          whatsappNumber: settings.whatsappNumber ?? '',
          instagramUrl: settings.instagramUrl ?? '',
          email: settings.contactEmail ?? '',
          address: settings.address ?? '',
          hours: settings.openingHours ?? '',
          logoUrl: settings.logoUrl ?? '',
          logoPublicId: settings.logoPublicId ?? '',
          publicSiteUrl: settings.publicSiteUrl ?? '',
          defaultSeoTitle: settings.defaultSeoTitle ?? '',
          defaultMetaDescription: settings.defaultMetaDescription ?? '',
          defaultSocialImageUrl: settings.defaultSocialImageUrl ?? '',
          country: settings.country ?? '',
          region: settings.region ?? '',
          city: settings.city ?? '',
          postalCode: settings.postalCode ?? '',
          googleSiteVerification: settings.googleSiteVerification ?? '',
          bingSiteVerification: settings.bingSiteVerification ?? '',
        });
        this.form.markAsPristine();
      },
      error: (error) => {
        this.state.set({
          status: 'error',
          message: apiErrorMessage(error, 'No se pudo cargar la configuración'),
        });
      },
    });
  }

  onLogoUploaded(response: MediaUploadResponse): void {
    this.logoUploadError.set('');
    this.form.patchValue({
      logoUrl: response.url,
      logoPublicId: response.publicId,
    });
    this.form.markAsDirty();
  }

  onLogoUploadFailed(message: string): void {
    this.logoUploadError.set(message);
  }

  clearLogo(): void {
    this.form.patchValue({ logoUrl: '', logoPublicId: '' });
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
    this.submitting.set(true);

    this.settingsApi
      .update({
        businessName: raw.businessName.trim(),
        whatsappNumber: raw.whatsappNumber.trim() || null,
        instagramUrl: raw.instagramUrl.trim() || null,
        contactEmail: raw.email.trim() || null,
        address: raw.address.trim() || null,
        openingHours: raw.hours.trim() || null,
        logoUrl: raw.logoUrl.trim() || null,
        logoPublicId: raw.logoPublicId.trim() || null,
        publicSiteUrl: raw.publicSiteUrl.trim() || null,
        defaultSeoTitle: raw.defaultSeoTitle.trim() || null,
        defaultMetaDescription: raw.defaultMetaDescription.trim() || null,
        defaultSocialImageUrl: raw.defaultSocialImageUrl.trim() || null,
        country: raw.country.trim() || null,
        region: raw.region.trim() || null,
        city: raw.city.trim() || null,
        postalCode: raw.postalCode.trim() || null,
        googleSiteVerification: raw.googleSiteVerification.trim() || null,
        bingSiteVerification: raw.bingSiteVerification.trim() || null,
      })
      .subscribe({
        next: (settings) => {
          this.submitting.set(false);
          this.form.patchValue({
            logoUrl: settings.logoUrl ?? '',
            logoPublicId: settings.logoPublicId ?? '',
          });
          this.form.markAsPristine();
          this.state.set(successState(settings));
          this.successMessage.set('Configuración guardada correctamente.');
          scrollToTop();
        },
        error: (error) => {
          this.submitting.set(false);
          const parsed = parseApiError(error);
          this.submitError.set(apiErrorMessage(error, 'No se pudo guardar la configuración'));
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
    if (control.hasError('required')) {
      return 'Campo obligatorio';
    }
    if (control.hasError('email')) {
      return 'Email inválido';
    }
    return 'Valor inválido';
  }
}
