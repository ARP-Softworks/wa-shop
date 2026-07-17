import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface BreadcrumbItem {
  label: string;
  link?: string | string[];
}

@Component({
  selector: 'app-admin-breadcrumbs',
  standalone: true,
  imports: [RouterLink],
  template: `
    <nav class="breadcrumbs" aria-label="Miga de pan">
      @for (item of items; track item.label; let last = $last) {
        @if (!last && item.link) {
          <a [routerLink]="item.link">{{ item.label }}</a>
          <span class="sep" aria-hidden="true">/</span>
        } @else {
          <span [class.current]="last">{{ item.label }}</span>
        }
      }
    </nav>
  `,
  styles: [
    `
      .breadcrumbs {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.35rem;
        font-size: 0.9rem;
        color: var(--color-muted);
        margin-bottom: var(--space-2);
      }

      .breadcrumbs a {
        color: var(--color-accent);
        text-decoration: none;
      }

      .breadcrumbs a:hover {
        text-decoration: underline;
      }

      .current {
        color: var(--color-ink);
        font-weight: 600;
      }

      .sep {
        opacity: 0.5;
      }
    `,
  ],
})
export class AdminBreadcrumbsComponent {
  @Input({ required: true }) items: BreadcrumbItem[] = [];
}
