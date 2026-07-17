import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminOrderDetail,
  AdminOrderSearchParams,
  AdminOrderSummary,
  OrderStatusUpdateRequest,
  PageResponse,
} from '../../shared/models/admin.models';
import { toHttpParams } from './admin-http.util';

@Injectable({ providedIn: 'root' })
export class AdminOrderApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/orders`;

  search(params: AdminOrderSearchParams): Observable<PageResponse<AdminOrderSummary>> {
    return this.http.get<PageResponse<AdminOrderSummary>>(this.baseUrl, {
      params: toHttpParams(params as Record<string, string | number | boolean | undefined | null>),
    });
  }

  getById(id: string): Observable<AdminOrderDetail> {
    return this.http.get<AdminOrderDetail>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  updateStatus(id: string, body: OrderStatusUpdateRequest): Observable<AdminOrderDetail> {
    return this.http.patch<AdminOrderDetail>(`${this.baseUrl}/${encodeURIComponent(id)}/status`, body);
  }
}
