import { Component, Input } from '@angular/core';
import { UiState } from '../../models/ui-state';

@Component({
  selector: 'app-state-panel',
  standalone: true,
  template: `
    @switch (state.status) {
      @case ('loading') {
        <div class="panel panel--loading" role="status">Cargando…</div>
      }
      @case ('empty') {
        <div class="panel panel--empty">{{ state.message || 'Sin datos' }}</div>
      }
      @case ('error') {
        <div class="panel panel--error" role="alert">{{ state.message || 'Error' }}</div>
      }
    }
  `,
  styles: [
    `
      .panel {
        padding: 1rem 1.25rem;
        border-radius: var(--radius);
        border: 1px solid var(--color-border);
        background: var(--color-surface);
        color: var(--color-muted);
      }

      .panel--error {
        border-color: color-mix(in srgb, var(--color-danger) 35%, white);
        color: var(--color-danger);
      }

      .panel--empty {
        color: var(--color-muted);
      }
    `,
  ],
})
export class StatePanelComponent {
  @Input({ required: true }) state!: UiState;
}
