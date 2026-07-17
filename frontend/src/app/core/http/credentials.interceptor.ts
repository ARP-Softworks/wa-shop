import { HttpInterceptorFn } from '@angular/common/http';

/** Sends cookies (session + XSRF) on every API request. */
export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.includes('/api')) {
    return next(req);
  }

  return next(
    req.clone({
      withCredentials: true,
    })
  );
};
