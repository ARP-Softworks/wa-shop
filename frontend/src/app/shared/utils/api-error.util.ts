import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorResponse } from '../models/admin.models';

export function parseApiError(error: unknown): ApiErrorResponse | null {
  if (!(error instanceof HttpErrorResponse) || !error.error || typeof error.error !== 'object') {
    return null;
  }
  return error.error as ApiErrorResponse;
}

export function apiErrorMessage(error: unknown, fallback = 'Ocurrió un error'): string {
  const parsed = parseApiError(error);
  return parsed?.message ?? fallback;
}

/** Maps "fieldName: message" entries from backend validation to form field keys. */
export function mapFieldErrors(details: string[] | undefined): Record<string, string> {
  if (!details?.length) {
    return {};
  }

  const result: Record<string, string> = {};
  for (const detail of details) {
    const separator = detail.indexOf(':');
    if (separator === -1) {
      continue;
    }
    const field = detail.slice(0, separator).trim();
    const message = detail.slice(separator + 1).trim();
    if (field) {
      result[field] = message;
    }
  }
  return result;
}
