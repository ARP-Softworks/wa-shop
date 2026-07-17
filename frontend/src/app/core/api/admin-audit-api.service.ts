import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuditLog, PageResponse } from '../../shared/models/admin.models';
import { toHttpParams } from './admin-http.util';

export interface AuditSearchParams {
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class AdminAuditApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/audit-logs`;

  list(params: AuditSearchParams = {}): Observable<PageResponse<AuditLog>> {
    return this.http.get<PageResponse<AuditLog>>(this.baseUrl, {
      params: toHttpParams(params as Record<string, string | number | boolean | undefined | null>),
    });
  }
}
