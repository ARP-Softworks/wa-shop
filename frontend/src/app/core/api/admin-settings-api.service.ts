import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SiteSettings, SiteSettingsWriteRequest } from '../../shared/models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminSettingsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/settings`;

  get(): Observable<SiteSettings> {
    return this.http.get<SiteSettings>(this.baseUrl);
  }

  update(body: SiteSettingsWriteRequest): Observable<SiteSettings> {
    return this.http.put<SiteSettings>(this.baseUrl, body);
  }
}
