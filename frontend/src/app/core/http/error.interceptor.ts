import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        const isAuthEndpoint =
          req.url.includes('/api/auth/login') ||
          req.url.includes('/api/auth/csrf') ||
          req.url.includes('/api/auth/me');

        auth.clearClientSession();

        if (!isAuthEndpoint && !req.url.includes('/api/public/')) {
          void router.navigate(['/admin/login']);
        }
      }
      return throwError(() => error);
    })
  );
};
