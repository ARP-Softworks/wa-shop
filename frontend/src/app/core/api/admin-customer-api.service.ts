import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminCustomer,
  AdminCustomerSearchParams,
  CustomerWriteRequest,
  PageResponse,
} from '../../shared/models/admin.models';
import { toHttpParams } from './admin-http.util';

@Injectable({ providedIn: 'root' })
export class AdminCustomerApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/customers`;

  search(params: AdminCustomerSearchParams): Observable<PageResponse<AdminCustomer>> {
    return this.http.get<PageResponse<AdminCustomer>>(this.baseUrl, {
      params: toHttpParams(params as Record<string, string | number | boolean | undefined | null>),
    });
  }

  getById(id: string): Observable<AdminCustomer> {
    return this.http.get<AdminCustomer>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  create(body: CustomerWriteRequest): Observable<AdminCustomer> {
    return this.http.post<AdminCustomer>(this.baseUrl, body);
  }

  update(id: string, body: CustomerWriteRequest): Observable<AdminCustomer> {
    return this.http.put<AdminCustomer>(`${this.baseUrl}/${encodeURIComponent(id)}`, body);
  }
}
