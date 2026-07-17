export type UiStateStatus = 'idle' | 'loading' | 'success' | 'empty' | 'error';

export interface UiState<T = unknown> {
  status: UiStateStatus;
  data?: T;
  message?: string;
}

export function loadingState<T>(): UiState<T> {
  return { status: 'loading' };
}

export function successState<T>(data: T): UiState<T> {
  return { status: 'success', data };
}

export function emptyState<T>(message = 'No hay resultados'): UiState<T> {
  return { status: 'empty', message };
}

export function errorState<T>(message = 'Ocurrió un error'): UiState<T> {
  return { status: 'error', message };
}
