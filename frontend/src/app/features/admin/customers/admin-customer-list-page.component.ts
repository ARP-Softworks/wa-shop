import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AdminCustomerApiService } from '../../../core/api/admin-customer-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminCustomer, PageResponse } from '../../../shared/models/admin.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-admin-customer-list-page',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, RouterLink, AdminBreadcrumbsComponent, StatePanelComponent],
  templateUrl: './admin-customer-list-page.component.html',
  styleUrl: './admin-customer-list-page.component.scss',
})
export class AdminCustomerListPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly customerApi = inject(AdminCustomerApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly state = signal<UiState<PageResponse<AdminCustomer>>>(loadingState());
  readonly page = signal(0);
  readonly pageSize = 20;

  readonly filters = this.fb.nonNullable.group({
    q: [''],
  });

  ngOnInit(): void {
    this.filters.controls.q.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.page.set(0);
        this.load();
      });
    this.load();
  }

  load(): void {
    this.state.set(loadingState());
    this.customerApi
      .search({ q: this.filters.controls.q.value || undefined, page: this.page(), size: this.pageSize })
      .subscribe({
        next: (response) => {
          this.state.set(
            response.content.length === 0 && this.page() === 0
              ? { status: 'empty', message: 'No hay clientes registrados todavía.' }
              : successState(response)
          );
        },
        error: (error) => {
          this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar los clientes')));
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
