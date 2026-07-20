import { UiSelectOption } from '../components/ui-select/ui-select.component';

export const INQUIRY_STATUS_OPTIONS: UiSelectOption[] = [
  { value: 'NEW', label: 'Nueva' },
  { value: 'IN_PROGRESS', label: 'En progreso' },
  { value: 'RESPONDED', label: 'Respondida' },
  { value: 'CLOSED', label: 'Cerrada' },
];

export const INQUIRY_STATUS_FILTER_OPTIONS: UiSelectOption[] = [
  { value: '', label: 'Todos' },
  ...INQUIRY_STATUS_OPTIONS,
];
