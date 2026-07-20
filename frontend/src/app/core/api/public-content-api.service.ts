import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Category,
  PublicHeroBanner,
  PublicPromotion,
  PublicSiteSettings,
  TechnicalServiceItem,
} from '../../shared/models/catalog.models';

@Injectable({ providedIn: 'root' })
export class PublicContentApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/public`;

  readonly settings = signal<PublicSiteSettings | null>(null);

  loadSettings(): Observable<PublicSiteSettings | null> {
    return this.http.get<PublicSiteSettings>(`${this.baseUrl}/settings`).pipe(
      tap((settings) => this.settings.set(settings)),
      catchError(() => {
        this.settings.set(null);
        return of(null);
      })
    );
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`).pipe(
      catchError(() => of([]))
    );
  }

  getTechnicalServices(): Observable<TechnicalServiceItem[]> {
    return this.http.get<TechnicalServiceItem[]>(`${this.baseUrl}/technical-services`).pipe(
      catchError(() => of([]))
    );
  }

  getTechnicalServiceBySlug(slug: string): Observable<TechnicalServiceItem> {
    return this.http.get<TechnicalServiceItem>(
      `${this.baseUrl}/technical-services/${encodeURIComponent(slug)}`
    );
  }

  getPromotions(): Observable<PublicPromotion[]> {
    return this.http.get<PublicPromotion[]>(`${this.baseUrl}/promotions`).pipe(
      catchError(() => of([]))
    );
  }

  getHeroBanners(): Observable<PublicHeroBanner[]> {
    return this.http.get<PublicHeroBanner[]>(`${this.baseUrl}/hero-banners`).pipe(
      catchError(() => of([]))
    );
  }
}
