import { Pipe, PipeTransform } from '@angular/core';
import { OrderStatus } from '../models/admin.models';

@Pipe({ name: 'orderStatusLabel', standalone: true })
export class OrderStatusLabelPipe implements PipeTransform {
  transform(value: OrderStatus | null | undefined): string {
    switch (value) {
      case 'PENDING_PAYMENT':
        return 'Pendiente de pago';
      case 'PAID':
        return 'Pagado';
      case 'CONFIRMED':
        return 'Confirmado';
      case 'SHIPPED':
        return 'Enviado';
      case 'DELIVERED':
        return 'Entregado';
      case 'CANCELLED':
        return 'Cancelado';
      case 'REJECTED':
        return 'Rechazado';
      case 'EXPIRED':
        return 'Expirado';
      default:
        return '';
    }
  }
}
