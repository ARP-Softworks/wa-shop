import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminPromotion, PromotionWriteRequest } from '../../shared/models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminPromotionApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/promotions`;

  list(): Observable<AdminPromotion[]> {
    return this.http.get<AdminPromotion[]>(this.baseUrl);
  }

  getById(id: string): Observable<AdminPromotion> {
    return this.http.get<AdminPromotion>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  create(body: PromotionWriteRequest): Observable<AdminPromotion> {
    return this.http.post<AdminPromotion>(this.baseUrl, body);
  }

  update(id: string, body: PromotionWriteRequest): Observable<AdminPromotion> {
    return this.http.put<AdminPromotion>(`${this.baseUrl}/${encodeURIComponent(id)}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }
}
