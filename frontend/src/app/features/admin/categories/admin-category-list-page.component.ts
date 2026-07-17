import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminCategoryApiService } from '../../../core/api/admin-category-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminCategory } from '../../../shared/models/admin.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';

@Component({
  selector: 'app-admin-category-list-page',
  standalone: true,
  imports: [RouterLink, AdminBreadcrumbsComponent, StatePanelComponent],
  templateUrl: './admin-category-list-page.component.html',
  styleUrl: './admin-category-list-page.component.scss',
})
export class AdminCategoryListPageComponent implements OnInit {
  private readonly categoryApi = inject(AdminCategoryApiService);

  readonly state = signal<UiState<AdminCategory[]>>(loadingState());
  readonly actionError = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.actionError.set('');
    this.state.set(loadingState());
    this.categoryApi.list().subscribe({
      next: (items) => {
        this.state.set(
          items.length === 0
            ? { status: 'empty', message: 'No hay categorías registradas.' }
            : successState(items)
        );
      },
      error: (error) => {
        this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar las categorías')));
      },
    });
  }

  deleteCategory(category: AdminCategory): void {
    if (!confirmAction(`¿Eliminar la categoría "${category.name}"?`)) {
      return;
    }
    this.categoryApi.delete(category.id).subscribe({
      next: () => this.load(),
      error: (error) => {
        this.actionError.set(apiErrorMessage(error, 'No se pudo eliminar la categoría'));
      },
    });
  }
}
