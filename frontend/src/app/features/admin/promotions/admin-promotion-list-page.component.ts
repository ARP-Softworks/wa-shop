import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminPromotionApiService } from '../../../core/api/admin-promotion-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminPromotion } from '../../../shared/models/admin.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';

@Component({
  selector: 'app-admin-promotion-list-page',
  standalone: true,
  imports: [RouterLink, AdminBreadcrumbsComponent, StatePanelComponent],
  templateUrl: './admin-promotion-list-page.component.html',
  styleUrl: './admin-promotion-list-page.component.scss',
})
export class AdminPromotionListPageComponent implements OnInit {
  private readonly promotionApi = inject(AdminPromotionApiService);

  readonly state = signal<UiState<AdminPromotion[]>>(loadingState());
  readonly actionError = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.actionError.set('');
    this.state.set(loadingState());
    this.promotionApi.list().subscribe({
      next: (items) => {
        this.state.set(
          items.length === 0
            ? { status: 'empty', message: 'No hay promociones cargadas todavía.' }
            : successState(items)
        );
      },
      error: (error) => {
        this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar las promociones')));
      },
    });
  }

  deletePromotion(promotion: AdminPromotion): void {
    if (!confirmAction(`¿Eliminar la promoción "${promotion.name}"?`)) {
      return;
    }
    this.promotionApi.delete(promotion.id).subscribe({
      next: () => this.load(),
      error: (error) => {
        this.actionError.set(apiErrorMessage(error, 'No se pudo eliminar la promoción'));
      },
    });
  }
}
