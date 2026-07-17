import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminCategory, CategoryWriteRequest } from '../../shared/models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminCategoryApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/categories`;

  list(): Observable<AdminCategory[]> {
    return this.http.get<AdminCategory[]>(this.baseUrl);
  }

  getById(id: string): Observable<AdminCategory> {
    return this.http.get<AdminCategory>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  create(body: CategoryWriteRequest): Observable<AdminCategory> {
    return this.http.post<AdminCategory>(this.baseUrl, body);
  }

  update(id: string, body: CategoryWriteRequest): Observable<AdminCategory> {
    return this.http.put<AdminCategory>(`${this.baseUrl}/${encodeURIComponent(id)}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }
}
