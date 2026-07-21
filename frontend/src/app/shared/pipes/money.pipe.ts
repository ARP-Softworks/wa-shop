import { Pipe, PipeTransform } from '@angular/core';
import { CurrencyCode } from '../models/catalog.models';

@Pipe({ name: 'money', standalone: true })
export class MoneyPipe implements PipeTransform {
  transform(value: number | null | undefined, currency: CurrencyCode | string = 'UYU'): string {
    if (value == null) {
      return '—';
    }
    // UYU prices are always whole pesos in this store, so cents are omitted; foreign
    // currencies (USD) always show 2 decimals — matches how they're actually priced.
    const isUyu = String(currency) === 'UYU';
    return new Intl.NumberFormat('es-UY', {
      style: 'currency',
      currency: String(currency),
      minimumFractionDigits: isUyu ? 0 : 2,
      maximumFractionDigits: 2,
    }).format(value);
  }
}
