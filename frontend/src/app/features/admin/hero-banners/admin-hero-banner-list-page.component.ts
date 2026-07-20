import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminHeroBannerApiService } from '../../../core/api/admin-hero-banner-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminHeroBanner } from '../../../shared/models/admin.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';

@Component({
  selector: 'app-admin-hero-banner-list-page',
  standalone: true,
  imports: [RouterLink, AdminBreadcrumbsComponent, StatePanelComponent],
  templateUrl: './admin-hero-banner-list-page.component.html',
  styleUrl: './admin-hero-banner-list-page.component.scss',
})
export class AdminHeroBannerListPageComponent implements OnInit {
  private readonly bannerApi = inject(AdminHeroBannerApiService);

  readonly state = signal<UiState<AdminHeroBanner[]>>(loadingState());
  readonly actionError = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.actionError.set('');
    this.state.set(loadingState());
    this.bannerApi.list().subscribe({
      next: (items) => {
        this.state.set(
          items.length === 0
            ? { status: 'empty', message: 'No hay banners cargados todavía.' }
            : successState(items)
        );
      },
      error: (error) => {
        this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar los banners')));
      },
    });
  }

  deleteBanner(banner: AdminHeroBanner): void {
    if (!confirmAction('¿Eliminar este banner?')) {
      return;
    }
    this.bannerApi.delete(banner.id).subscribe({
      next: () => this.load(),
      error: (error) => {
        this.actionError.set(apiErrorMessage(error, 'No se pudo eliminar el banner'));
      },
    });
  }
}
