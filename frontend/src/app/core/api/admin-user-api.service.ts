import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminUser,
  AdminUserSearchParams,
  PageResponse,
  UserWriteRequest,
} from '../../shared/models/admin.models';
import { toHttpParams } from './admin-http.util';

@Injectable({ providedIn: 'root' })
export class AdminUserApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/users`;

  search(params: AdminUserSearchParams): Observable<PageResponse<AdminUser>> {
    return this.http.get<PageResponse<AdminUser>>(this.baseUrl, {
      params: toHttpParams(params as Record<string, string | number | boolean | undefined | null>),
    });
  }

  getById(id: string): Observable<AdminUser> {
    return this.http.get<AdminUser>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  create(body: UserWriteRequest): Observable<AdminUser> {
    return this.http.post<AdminUser>(this.baseUrl, body);
  }

  update(id: string, body: UserWriteRequest): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.baseUrl}/${encodeURIComponent(id)}`, body);
  }
}
