import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PublicSeoConfigApiResponse, SeoPageMetaApiResponse } from '../../shared/models/seo.models';

@Injectable({ providedIn: 'root' })
export class SeoApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/public/seo`;

  getConfig(): Observable<PublicSeoConfigApiResponse> {
    return this.http.get<PublicSeoConfigApiResponse>(`${this.baseUrl}/config`);
  }

  getPageMeta(path: string): Observable<SeoPageMetaApiResponse> {
    const params = new HttpParams().set('path', path.startsWith('/') ? path : `/${path}`);
    return this.http.get<SeoPageMetaApiResponse>(`${this.baseUrl}/page`, { params });
  }
}
