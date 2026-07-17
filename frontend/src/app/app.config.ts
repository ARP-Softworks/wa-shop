import { ApplicationConfig, inject, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { NavigationEnd, provideRouter, Router } from '@angular/router';
import { provideHttpClient, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import { filter } from 'rxjs';

import { routes } from './app.routes';
import { credentialsInterceptor } from './core/http/credentials.interceptor';
import { errorInterceptor } from './core/http/error.interceptor';
import { SeoService } from './core/seo/seo.service';
import { AnalyticsService } from './core/analytics/analytics.service';

function initSeoAndAnalytics(): void {
  const router = inject(Router);
  const seo = inject(SeoService);
  const analytics = inject(AnalyticsService);

  analytics.init();

  router.events.pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd)).subscribe((event) => {
    const path = event.urlAfterRedirects.split('?')[0] || '/';
    seo.applyFromPath(path);
    analytics.trackPageView(path);
  });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      }),
      withInterceptors([credentialsInterceptor, errorInterceptor])
    ),
    provideAppInitializer(() => {
      initSeoAndAnalytics();
    }),
  ],
};
