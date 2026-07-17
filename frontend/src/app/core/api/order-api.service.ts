import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { OrderCreateRequest, OrderCreateResponse, OrderStatusResponse } from '../../shared/models/catalog.models';

@Injectable({ providedIn: 'root' })
export class OrderApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/public/orders`;

  create(request: OrderCreateRequest): Observable<OrderCreateResponse> {
    return this.http.post<OrderCreateResponse>(this.baseUrl, request);
  }

  getStatus(orderId: string): Observable<OrderStatusResponse> {
    return this.http.get<OrderStatusResponse>(`${this.baseUrl}/${encodeURIComponent(orderId)}/status`);
  }
}
