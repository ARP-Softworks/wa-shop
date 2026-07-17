import { Pipe, PipeTransform } from '@angular/core';
import { CurrencyCode } from '../models/catalog.models';

@Pipe({ name: 'money', standalone: true })
export class MoneyPipe implements PipeTransform {
  transform(value: number | null | undefined, currency: CurrencyCode | string = 'UYU'): string {
    if (value == null) {
      return '—';
    }
    return new Intl.NumberFormat('es-UY', {
      style: 'currency',
      currency: String(currency),
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(value);
  }
}
