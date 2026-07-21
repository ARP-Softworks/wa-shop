import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    dataLayer?: unknown[];
    gtag?: (...args: unknown[]) => void;
  }
}

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly document = inject(DOCUMENT);
  private readonly platformId = inject(PLATFORM_ID);
  private loaded = false;

  private get gaId(): string {
    return (environment.googleAnalyticsId || '').trim();
  }

  private get gtmId(): string {
    return (environment.googleTagManagerId || '').trim();
  }

  private get enabled(): boolean {
    return isPlatformBrowser(this.platformId) && (!!this.gaId || !!this.gtmId);
  }

  init(): void {
    if (!this.enabled || this.loaded) {
      return;
    }
    this.loaded = true;

    if (this.gtmId) {
      this.loadGtm(this.gtmId);
    }
    if (this.gaId) {
      this.loadGa(this.gaId);
    }
  }

  trackPageView(path: string): void {
    if (!this.enabled) {
      return;
    }
    this.init();
    const pagePath = path.startsWith('/') ? path : `/${path}`;
    if (typeof window.gtag === 'function' && this.gaId) {
      window.gtag('event', 'page_view', { page_path: pagePath });
    }
    if (this.gtmId && Array.isArray(window.dataLayer)) {
      window.dataLayer.push({ event: 'page_view', page_path: pagePath });
    }
  }

  trackProductView(_productId: string, _slug?: string): void {
    if (!this.enabled) {
      return;
    }
    this.init();
    if (typeof window.gtag === 'function' && this.gaId) {
      window.gtag('event', 'view_item', { item_id: _productId, item_slug: _slug });
    }
    if (this.gtmId && Array.isArray(window.dataLayer)) {
      window.dataLayer.push({ event: 'view_item', item_id: _productId, item_slug: _slug });
    }
  }

  trackWhatsappClick(_context?: string): void {
    if (!this.enabled) {
      return;
    }
    this.init();
    if (typeof window.gtag === 'function' && this.gaId) {
      window.gtag('event', 'whatsapp_click', { context: _context });
    }
    if (this.gtmId && Array.isArray(window.dataLayer)) {
      window.dataLayer.push({ event: 'whatsapp_click', context: _context });
    }
  }

  trackAddToCart(_context?: string): void {
    if (!this.enabled) {
      return;
    }
    this.init();
    if (typeof window.gtag === 'function' && this.gaId) {
      window.gtag('event', 'add_to_cart', { context: _context });
    }
    if (this.gtmId && Array.isArray(window.dataLayer)) {
      window.dataLayer.push({ event: 'add_to_cart', context: _context });
    }
  }

  trackSearch(_query: string): void {
    if (!this.enabled) {
      return;
    }
    this.init();
    if (typeof window.gtag === 'function' && this.gaId) {
      window.gtag('event', 'search', { search_term: _query });
    }
    if (this.gtmId && Array.isArray(window.dataLayer)) {
      window.dataLayer.push({ event: 'search', search_term: _query });
    }
  }

  private loadGa(id: string): void {
    const win = this.document.defaultView;
    if (!win) {
      return;
    }
    win.dataLayer = win.dataLayer || [];
    win.gtag = function gtag(...args: unknown[]) {
      win.dataLayer!.push(args);
    };
    win.gtag('js', new Date());
    win.gtag('config', id, { send_page_view: false });

    const script = this.document.createElement('script');
    script.async = true;
    script.src = `https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(id)}`;
    this.document.head.appendChild(script);
  }

  private loadGtm(id: string): void {
    const win = this.document.defaultView;
    if (!win) {
      return;
    }
    win.dataLayer = win.dataLayer || [];
    win.dataLayer.push({ 'gtm.start': Date.now(), event: 'gtm.js' });

    const script = this.document.createElement('script');
    script.async = true;
    script.src = `https://www.googletagmanager.com/gtm.js?id=${encodeURIComponent(id)}`;
    this.document.head.appendChild(script);
  }
}
