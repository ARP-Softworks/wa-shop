import { DatePipe, NgClass } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { InquiryStatus } from '../../../shared/models/admin.models';
import { AdminDashboardApiService } from '../../../core/api/admin-dashboard-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { DashboardResponse } from '../../../shared/models/admin.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { ConditionLabelPipe } from '../../../shared/pipes/condition-label.pipe';
import { InquiryStatusLabelPipe } from '../../../shared/pipes/inquiry-status-label.pipe';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-admin-dashboard-page',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    RouterLink,
    AdminBreadcrumbsComponent,
    StatePanelComponent,
    ConditionLabelPipe,
    InquiryStatusLabelPipe,
    MoneyPipe,
  ],
  templateUrl: './admin-dashboard-page.component.html',
  styleUrl: './admin-dashboard-page.component.scss',
})
export class AdminDashboardPageComponent implements OnInit {
  private readonly dashboardApi = inject(AdminDashboardApiService);

  readonly state = signal<UiState<DashboardResponse>>(loadingState());
  readonly breadcrumbs = [{ label: 'Panel' }];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.state.set(loadingState());
    this.dashboardApi.getDashboard().subscribe({
      next: (data) => {
        this.state.set(successState(data));
      },
      error: (error) => {
        this.state.set(errorState(apiErrorMessage(error, 'No se pudo cargar el panel')));
      },
    });
  }

  statusClass(status: InquiryStatus): Record<string, boolean> {
    return {
      'status-badge--new': status === 'NEW',
      'status-badge--progress': status === 'IN_PROGRESS',
      'status-badge--responded': status === 'RESPONDED',
      'status-badge--closed': status === 'CLOSED',
    };
  }
}
