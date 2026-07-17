import { Pipe, PipeTransform } from '@angular/core';
import { ProductCondition } from '../models/catalog.models';

@Pipe({ name: 'conditionLabel', standalone: true })
export class ConditionLabelPipe implements PipeTransform {
  transform(value: ProductCondition | null | undefined): string {
    if (value === 'NEW') {
      return 'Nuevo';
    }
    if (value === 'USED') {
      return 'Usado';
    }
    return '';
  }
}
