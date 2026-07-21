import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AdminUserApiService } from '../../../core/api/admin-user-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { CanComponentDeactivate } from '../../../core/auth/can-deactivate.guard';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminUser } from '../../../shared/models/admin.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { scrollToTop } from '../../../shared/utils/scroll.util';

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password && confirmPassword && password !== confirmPassword ? { passwordsMismatch: true } : null;
}

@Component({
  selector: 'app-admin-user-form-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminBreadcrumbsComponent, StatePanelComponent],
  templateUrl: './admin-user-form-page.component.html',
  styleUrl: './admin-user-form-page.component.scss',
})
export class AdminUserFormPageComponent implements OnInit, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly userApi = inject(AdminUserApiService);
  readonly auth = inject(AuthService);

  readonly isEdit = signal(false);
  readonly userId = signal<string | null>(null);
  readonly loadState = signal<UiState<AdminUser>>(loadingState());
  readonly submitError = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly successMessage = signal('');
  readonly submitting = signal(false);

  readonly form = this.fb.nonNullable.group(
    {
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      password: [''],
      confirmPassword: [''],
      enabled: [true],
    },
    { validators: passwordsMatchValidator }
  );

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nuevo') {
      this.isEdit.set(true);
      this.userId.set(id);
      this.loadUser(id);
    } else {
      this.form.controls.password.addValidators([Validators.required, Validators.minLength(8)]);
      this.form.controls.confirmPassword.addValidators([Validators.required]);
      this.loadState.set(successState({} as AdminUser));
    }
  }

  get isSelf(): boolean {
    return this.isEdit() && this.auth.user()?.id === this.userId();
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
      email: body.email.trim(),
      firstName: body.firstName.trim(),
      lastName: body.lastName.trim(),
      enabled: body.enabled,
      password: body.password.trim() || null,
    };

    this.submitting.set(true);
    const request$ = this.isEdit()
      ? this.userApi.update(this.userId()!, payload)
      : this.userApi.create(payload);

    request$.subscribe({
      next: (user) => {
        this.submitting.set(false);
        this.form.patchValue({ password: '', confirmPassword: '' });
        this.form.markAsPristine();
        this.successMessage.set('Usuario guardado correctamente.');
        if (this.auth.user()?.id === user.id) {
          this.auth.updateCurrentUser({
            email: user.email,
            firstName: user.firstName,
            lastName: user.lastName,
          });
        }
        if (!this.isEdit()) {
          void this.router.navigate(['/admin/usuarios', user.id]);
        }
        scrollToTop();
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo guardar el usuario'));
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

  private loadUser(id: string): void {
    this.loadState.set(loadingState());
    this.userApi.getById(id).subscribe({
      next: (user) => {
        this.loadState.set(successState(user));
        this.form.reset({
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          password: '',
          confirmPassword: '',
          enabled: user.enabled,
        });
        this.form.markAsPristine();
        if (this.auth.user()?.id === user.id) {
          this.form.controls.enabled.disable();
        }
      },
      error: (error) => {
        this.loadState.set({ status: 'error', message: apiErrorMessage(error, 'No se pudo cargar el usuario') });
      },
    });
  }
}
