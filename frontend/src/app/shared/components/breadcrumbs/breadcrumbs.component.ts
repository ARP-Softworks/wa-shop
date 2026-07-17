import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface BreadcrumbItem {
  label: string;
  link?: string | string[];
}

@Component({
  selector: 'app-breadcrumbs',
  standalone: true,
  imports: [RouterLink],
  template: `
    <nav class="crumbs" aria-label="Miga de pan">
      <ol>
        @for (item of items; track item.label; let last = $last) {
          <li>
            @if (!last && item.link) {
              <a [routerLink]="item.link">{{ item.label }}</a>
            } @else {
              <span [attr.aria-current]="last ? 'page' : null">{{ item.label }}</span>
            }
          </li>
        }
      </ol>
    </nav>
  `,
  styles: [
    `
      .crumbs {
        margin-bottom: var(--space-3);
      }

      .crumbs ol {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-1) var(--space-2);
        list-style: none;
        margin: 0;
        padding: 0;
        font-size: 0.92rem;
        color: var(--color-muted);
      }

      .crumbs li:not(:last-child)::after {
        content: '/';
        margin-left: var(--space-2);
        color: var(--color-border);
      }

      .crumbs a {
        color: var(--color-accent);
      }

      .crumbs span[aria-current='page'] {
        color: var(--color-ink);
        font-weight: 600;
      }
    `,
  ],
})
export class BreadcrumbsComponent {
  @Input({ required: true }) items: BreadcrumbItem[] = [];
}
