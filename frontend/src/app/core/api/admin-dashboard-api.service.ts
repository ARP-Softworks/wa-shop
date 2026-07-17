import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardResponse } from '../../shared/models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminDashboardApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/dashboard`;

  getDashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(this.baseUrl);
  }
}
