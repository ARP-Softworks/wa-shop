import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PageResponse,
  ProductDetail,
  ProductSearchParams,
  ProductSummary,
} from '../../shared/models/catalog.models';

@Injectable({ providedIn: 'root' })
export class CatalogApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/public/products`;

  search(params: ProductSearchParams): Observable<PageResponse<ProductSummary>> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<PageResponse<ProductSummary>>(this.baseUrl, { params: httpParams });
  }

  getBySlug(slugOrId: string): Observable<ProductDetail> {
    return this.http.get<ProductDetail>(`${this.baseUrl}/${encodeURIComponent(slugOrId)}`);
  }

  related(slugOrId: string): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(
      `${this.baseUrl}/${encodeURIComponent(slugOrId)}/related`
    );
  }

  variants(slugOrId: string): Observable<ProductSummary[]> {
    return this.http.get<ProductSummary[]>(
      `${this.baseUrl}/${encodeURIComponent(slugOrId)}/variants`
    );
  }
}
