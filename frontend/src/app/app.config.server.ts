import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering } from '@angular/platform-server';
import { appConfig } from './app.config';

/**
 * Server config prepared for Angular prerender/SSR.
 *
 * Prerender is currently DISABLED in angular.json because route extraction
 * failed with NG0401 during `ng build` (likely lazy public routes + no live API).
 * Client SeoService + Spring SeoHtmlDocumentFilter remain the production SEO path.
 *
 * To retry later, set in angular.json build options:
 *   "server": "src/main.server.ts",
 *   "prerender": { "routesFile": "prerender-routes.txt" }
 * See also frontend/prerender-routes.txt and src/main.server.ts.
 */
const serverConfig: ApplicationConfig = {
  providers: [provideServerRendering()],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
