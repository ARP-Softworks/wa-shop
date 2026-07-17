import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminProductDetail,
  AdminProductSearchParams,
  AdminProductSummary,
  PageResponse,
  ProductWriteRequest,
} from '../../shared/models/admin.models';
import { toHttpParams } from './admin-http.util';

@Injectable({ providedIn: 'root' })
export class AdminProductApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/products`;

  search(params: AdminProductSearchParams): Observable<PageResponse<AdminProductSummary>> {
    return this.http.get<PageResponse<AdminProductSummary>>(this.baseUrl, {
      params: toHttpParams(params as Record<string, string | number | boolean | undefined | null>),
    });
  }

  getById(id: string): Observable<AdminProductDetail> {
    return this.http.get<AdminProductDetail>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  create(body: ProductWriteRequest): Observable<AdminProductDetail> {
    return this.http.post<AdminProductDetail>(this.baseUrl, body);
  }

  update(id: string, body: ProductWriteRequest): Observable<AdminProductDetail> {
    return this.http.put<AdminProductDetail>(`${this.baseUrl}/${encodeURIComponent(id)}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  setPublished(id: string, published: boolean): Observable<AdminProductDetail> {
    return this.http.patch<AdminProductDetail>(`${this.baseUrl}/${encodeURIComponent(id)}/publish`, {
      published,
    });
  }
}
