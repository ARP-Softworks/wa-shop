import { HttpParams } from '@angular/common/http';

export function toHttpParams(params: Record<string, string | number | boolean | undefined | null>): HttpParams {
  let httpParams = new HttpParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      httpParams = httpParams.set(key, String(value));
    }
  });
  return httpParams;
}
