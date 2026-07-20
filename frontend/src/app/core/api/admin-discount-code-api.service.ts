import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminDiscountCode, DiscountCodeWriteRequest } from '../../shared/models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminDiscountCodeApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/discount-codes`;

  list(): Observable<AdminDiscountCode[]> {
    return this.http.get<AdminDiscountCode[]>(this.baseUrl);
  }

  getById(id: string): Observable<AdminDiscountCode> {
    return this.http.get<AdminDiscountCode>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  create(body: DiscountCodeWriteRequest): Observable<AdminDiscountCode> {
    return this.http.post<AdminDiscountCode>(this.baseUrl, body);
  }

  update(id: string, body: DiscountCodeWriteRequest): Observable<AdminDiscountCode> {
    return this.http.put<AdminDiscountCode>(`${this.baseUrl}/${encodeURIComponent(id)}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }
}
