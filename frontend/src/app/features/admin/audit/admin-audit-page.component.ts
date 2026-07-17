import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { AdminAuditApiService } from '../../../core/api/admin-audit-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AuditLog, PageResponse } from '../../../shared/models/admin.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { AuditActionLabelPipe } from '../../../shared/pipes/audit-action-label.pipe';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-admin-audit-page',
  standalone: true,
  imports: [DatePipe, AdminBreadcrumbsComponent, StatePanelComponent, AuditActionLabelPipe],
  templateUrl: './admin-audit-page.component.html',
  styleUrl: './admin-audit-page.component.scss',
})
export class AdminAuditPageComponent implements OnInit {
  private readonly auditApi = inject(AdminAuditApiService);

  readonly state = signal<UiState<PageResponse<AuditLog>>>(loadingState());
  readonly page = signal(0);
  readonly pageSize = 20;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.state.set(loadingState());
    this.auditApi.list({ page: this.page(), size: this.pageSize }).subscribe({
      next: (response) => {
        this.state.set(
          response.content.length === 0 && this.page() === 0
            ? { status: 'empty', message: 'No hay registros de auditoría.' }
            : successState(response)
        );
      },
      error: (error) => {
        this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar los registros')));
      },
    });
  }

  goToPage(nextPage: number): void {
    const data = this.state().data;
    if (!data || nextPage < 0 || nextPage >= data.totalPages) {
      return;
    }
    this.page.set(nextPage);
    this.load();
  }
}
