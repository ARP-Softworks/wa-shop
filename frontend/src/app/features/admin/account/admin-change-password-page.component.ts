import { Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { scrollToTop } from '../../../shared/utils/scroll.util';

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const newPassword = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return newPassword && confirmPassword && newPassword !== confirmPassword
    ? { passwordsMismatch: true }
    : null;
}

@Component({
  selector: 'app-admin-change-password-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AdminBreadcrumbsComponent],
  templateUrl: './admin-change-password-page.component.html',
  styleUrl: './admin-change-password-page.component.scss',
})
export class AdminChangePasswordPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);

  readonly form = this.fb.nonNullable.group(
    {
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatchValidator }
  );

  showCurrent = false;
  showNew = false;
  submitting = false;
  errorMessage = '';
  successMessage = '';

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.form.markAllAsTouched();

    if (this.form.invalid) {
      return;
    }

    this.submitting = true;
    const { currentPassword, newPassword } = this.form.getRawValue();

    this.auth.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.submitting = false;
        this.successMessage = 'Contraseña actualizada correctamente.';
        this.form.reset({ currentPassword: '', newPassword: '', confirmPassword: '' });
        scrollToTop();
      },
      error: (error: unknown) => {
        this.submitting = false;
        if (error instanceof HttpErrorResponse && error.status === 401) {
          this.errorMessage = 'La contraseña actual no es correcta.';
        } else {
          this.errorMessage = 'No se pudo actualizar la contraseña. Intentá de nuevo.';
        }
        scrollToTop();
      },
    });
  }
}
