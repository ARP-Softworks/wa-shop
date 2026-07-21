import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminTechnicalServiceApiService } from '../../../core/api/admin-technical-service-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminTechnicalService } from '../../../shared/models/admin.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { scrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-admin-technical-service-list-page',
  standalone: true,
  imports: [RouterLink, AdminBreadcrumbsComponent, StatePanelComponent, MoneyPipe],
  templateUrl: './admin-technical-service-list-page.component.html',
  styleUrl: './admin-technical-service-list-page.component.scss',
})
export class AdminTechnicalServiceListPageComponent implements OnInit {
  private readonly serviceApi = inject(AdminTechnicalServiceApiService);

  readonly state = signal<UiState<AdminTechnicalService[]>>(loadingState());
  readonly actionError = signal('');
  readonly actionSuccess = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.actionError.set('');
    this.state.set(loadingState());
    this.serviceApi.list().subscribe({
      next: (items) => {
        this.state.set(
          items.length === 0
            ? { status: 'empty', message: 'No hay servicios técnicos registrados.' }
            : successState(items)
        );
      },
      error: (error) => {
        this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar los servicios')));
      },
    });
  }

  deleteService(service: AdminTechnicalService): void {
    if (!confirmAction(`¿Eliminar el servicio "${service.name}"?`)) {
      return;
    }
    this.actionSuccess.set('');
    this.serviceApi.delete(service.id).subscribe({
      next: () => {
        this.actionSuccess.set('Servicio eliminado.');
        scrollToTop();
        this.load();
      },
      error: (error) => {
        this.actionError.set(apiErrorMessage(error, 'No se pudo eliminar el servicio'));
        scrollToTop();
      },
    });
  }
}
