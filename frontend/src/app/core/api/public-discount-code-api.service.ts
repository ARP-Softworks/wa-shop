import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DiscountType } from '../../shared/models/admin.models';

export interface DiscountCodeValidateRequest {
  code: string;
  eligibleSubtotal: number;
}

export interface DiscountCodeValidateResponse {
  code: string;
  discountType: DiscountType;
  discountValue: number;
  discountAmount: number;
  eligibleSubtotal: number;
}

@Injectable({ providedIn: 'root' })
export class PublicDiscountCodeApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/public/discount-codes`;

  validate(body: DiscountCodeValidateRequest): Observable<DiscountCodeValidateResponse> {
    return this.http.post<DiscountCodeValidateResponse>(`${this.baseUrl}/validate`, body);
  }
}
