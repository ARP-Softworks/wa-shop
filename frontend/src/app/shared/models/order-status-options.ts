import { UiSelectOption } from '../components/ui-select/ui-select.component';

export const ORDER_STATUS_OPTIONS: UiSelectOption[] = [
  { value: 'PENDING_PAYMENT', label: 'Pendiente de pago' },
  { value: 'PAID', label: 'Pagado' },
  { value: 'CONFIRMED', label: 'Confirmado' },
  { value: 'SHIPPED', label: 'Enviado' },
  { value: 'DELIVERED', label: 'Entregado' },
  { value: 'CANCELLED', label: 'Cancelado' },
  { value: 'REJECTED', label: 'Rechazado' },
  { value: 'EXPIRED', label: 'Expirado' },
];

export const ORDER_STATUS_FILTER_OPTIONS: UiSelectOption[] = [{ value: '', label: 'Todos' }, ...ORDER_STATUS_OPTIONS];
