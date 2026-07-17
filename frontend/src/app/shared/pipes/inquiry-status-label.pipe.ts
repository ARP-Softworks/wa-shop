import { Pipe, PipeTransform } from '@angular/core';
import { InquiryStatus } from '../models/admin.models';

@Pipe({ name: 'inquiryStatusLabel', standalone: true })
export class InquiryStatusLabelPipe implements PipeTransform {
  transform(value: InquiryStatus | null | undefined): string {
    switch (value) {
      case 'NEW':
        return 'Nueva';
      case 'IN_PROGRESS':
        return 'En progreso';
      case 'RESPONDED':
        return 'Respondida';
      case 'CLOSED':
        return 'Cerrada';
      default:
        return '';
    }
  }
}
