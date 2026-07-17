import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminInquiryDetail,
  AdminInquirySearchParams,
  AdminInquirySummary,
  InquiryStatusUpdateRequest,
  PageResponse,
} from '../../shared/models/admin.models';
import { toHttpParams } from './admin-http.util';

@Injectable({ providedIn: 'root' })
export class AdminInquiryApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/inquiries`;

  search(params: AdminInquirySearchParams): Observable<PageResponse<AdminInquirySummary>> {
    return this.http.get<PageResponse<AdminInquirySummary>>(this.baseUrl, {
      params: toHttpParams(params as Record<string, string | number | boolean | undefined | null>),
    });
  }

  getById(id: string): Observable<AdminInquiryDetail> {
    return this.http.get<AdminInquiryDetail>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  updateStatus(id: string, body: InquiryStatusUpdateRequest): Observable<AdminInquiryDetail> {
    return this.http.put<AdminInquiryDetail>(`${this.baseUrl}/${encodeURIComponent(id)}/status`, body);
  }
}
