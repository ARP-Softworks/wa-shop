import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpEventType, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { Observable, filter, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MediaUploadResponse } from '../../shared/models/admin.models';

export interface MediaUploadProgress {
  progress: number;
  response?: MediaUploadResponse;
}

@Injectable({ providedIn: 'root' })
export class AdminMediaApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/media`;

  upload(file: File, folder = 'uploads'): Observable<MediaUploadProgress> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('folder', folder);

    return this.http
      .post<MediaUploadResponse>(`${this.baseUrl}/upload`, formData, {
        reportProgress: true,
        observe: 'events',
      })
      .pipe(
        filter(
          (event): event is HttpProgressEvent | HttpResponse<MediaUploadResponse> =>
            event.type === HttpEventType.UploadProgress || event.type === HttpEventType.Response
        ),
        map((event) => {
          if (event.type === HttpEventType.UploadProgress) {
            const total = event.total ?? file.size;
            const progress = total > 0 ? Math.round((100 * event.loaded) / total) : 0;
            return { progress };
          }
          return { progress: 100, response: (event as HttpResponse<MediaUploadResponse>).body! };
        })
      );
  }

  delete(publicId: string): Observable<void> {
    return this.http.delete<void>(this.baseUrl, { body: { publicId } });
  }
}
