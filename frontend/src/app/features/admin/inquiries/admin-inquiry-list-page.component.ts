import { DatePipe, NgClass } from '@angular/common';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { AdminInquiryApiService } from '../../../core/api/admin-inquiry-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import {
  AdminInquirySummary,
  InquiryStatus,
  PageResponse,
} from '../../../shared/models/admin.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { InquiryStatusLabelPipe } from '../../../shared/pipes/inquiry-status-label.pipe';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-admin-inquiry-list-page',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    ReactiveFormsModule,
    RouterLink,
    AdminBreadcrumbsComponent,
    StatePanelComponent,
    InquiryStatusLabelPipe,
  ],
  templateUrl: './admin-inquiry-list-page.component.html',
  styleUrl: './admin-inquiry-list-page.component.scss',
})
export class AdminInquiryListPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly inquiryApi = inject(AdminInquiryApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly state = signal<UiState<PageResponse<AdminInquirySummary>>>(loadingState());
  readonly page = signal(0);
  readonly pageSize = 20;

  readonly filters = this.fb.nonNullable.group({
    status: ['' as '' | InquiryStatus],
  });

  ngOnInit(): void {
    this.filters.controls.status.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.page.set(0);
        this.load();
      });
    this.load();
  }

  load(): void {
    this.state.set(loadingState());
    const status = this.filters.controls.status.value;
    this.inquiryApi
      .search({
        status: status || undefined,
        page: this.page(),
        size: this.pageSize,
      })
      .subscribe({
        next: (response) => {
          this.state.set(
            response.content.length === 0 && this.page() === 0
              ? { status: 'empty', message: 'No hay consultas con ese filtro.' }
              : successState(response)
          );
        },
        error: (error) => {
          this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar las consultas')));
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

  statusClass(status: InquiryStatus): Record<string, boolean> {
    return {
      'status-badge--new': status === 'NEW',
      'status-badge--progress': status === 'IN_PROGRESS',
      'status-badge--responded': status === 'RESPONDED',
      'status-badge--closed': status === 'CLOSED',
    };
  }
}
