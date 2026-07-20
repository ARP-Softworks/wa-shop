import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminHeroBanner, HeroBannerWriteRequest } from '../../shared/models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminHeroBannerApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/hero-banners`;

  list(): Observable<AdminHeroBanner[]> {
    return this.http.get<AdminHeroBanner[]>(this.baseUrl);
  }

  getById(id: string): Observable<AdminHeroBanner> {
    return this.http.get<AdminHeroBanner>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  create(body: HeroBannerWriteRequest): Observable<AdminHeroBanner> {
    return this.http.post<AdminHeroBanner>(this.baseUrl, body);
  }

  update(id: string, body: HeroBannerWriteRequest): Observable<AdminHeroBanner> {
    return this.http.put<AdminHeroBanner>(`${this.baseUrl}/${encodeURIComponent(id)}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }
}
