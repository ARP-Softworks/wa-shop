import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminTechnicalService,
  TechnicalServiceWriteRequest,
} from '../../shared/models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminTechnicalServiceApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/technical-services`;

  list(): Observable<AdminTechnicalService[]> {
    return this.http.get<AdminTechnicalService[]>(this.baseUrl);
  }

  getById(id: string): Observable<AdminTechnicalService> {
    return this.http.get<AdminTechnicalService>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  create(body: TechnicalServiceWriteRequest): Observable<AdminTechnicalService> {
    return this.http.post<AdminTechnicalService>(this.baseUrl, body);
  }

  update(id: string, body: TechnicalServiceWriteRequest): Observable<AdminTechnicalService> {
    return this.http.put<AdminTechnicalService>(`${this.baseUrl}/${encodeURIComponent(id)}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }
}
