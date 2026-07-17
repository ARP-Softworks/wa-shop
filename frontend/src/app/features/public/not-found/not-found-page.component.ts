import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SeoService } from '../../../core/seo/seo.service';

@Component({
  selector: 'app-not-found-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="section container not-found">
      <p class="section__eyebrow">Error 404</p>
      <h1>Página no encontrada</h1>
      <p>El enlace no existe o el producto ya no está publicado.</p>
      <div class="actions">
        <a class="btn" routerLink="/">Volver al inicio</a>
        <a class="btn btn-secondary" routerLink="/iphone">Ver iPhone</a>
        <a class="btn btn-secondary" routerLink="/accesorios">Ver accesorios</a>
      </div>
    </section>
  `,
  styles: [
    `
      .not-found {
        text-align: center;
        max-width: 36rem;
        margin-inline: auto;
        padding-block: var(--space-6);
      }

      .actions {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-2);
        margin-top: var(--space-4);
        justify-content: center;
      }
    `,
  ],
})
export class NotFoundPageComponent implements OnInit {
  private readonly seo = inject(SeoService);

  ngOnInit(): void {
    this.seo.applyNotFound();
  }
}
