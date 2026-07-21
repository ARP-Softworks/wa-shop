import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminDiscountCodeApiService } from '../../../core/api/admin-discount-code-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { AdminDiscountCode } from '../../../shared/models/admin.models';
import { errorState, loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { scrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-admin-discount-code-list-page',
  standalone: true,
  imports: [RouterLink, AdminBreadcrumbsComponent, StatePanelComponent, DatePipe],
  templateUrl: './admin-discount-code-list-page.component.html',
  styleUrl: './admin-discount-code-list-page.component.scss',
})
export class AdminDiscountCodeListPageComponent implements OnInit {
  private readonly api = inject(AdminDiscountCodeApiService);

  readonly state = signal<UiState<AdminDiscountCode[]>>(loadingState());
  readonly actionError = signal('');
  readonly actionSuccess = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.actionError.set('');
    this.state.set(loadingState());
    this.api.list().subscribe({
      next: (items) => {
        this.state.set(
          items.length === 0
            ? { status: 'empty', message: 'No hay códigos de descuento todavía.' }
            : successState(items)
        );
      },
      error: (error) => {
        this.state.set(errorState(apiErrorMessage(error, 'No se pudieron cargar los códigos')));
      },
    });
  }

  formatValue(code: AdminDiscountCode): string {
    if (code.discountType === 'PERCENT') {
      return `${code.discountValue}%`;
    }
    return `UYU ${code.discountValue}`;
  }

  formatUses(code: AdminDiscountCode): string {
    return code.maxUses == null ? `${code.usedCount} / ∞` : `${code.usedCount} / ${code.maxUses}`;
  }

  deleteCode(code: AdminDiscountCode): void {
    if (!confirmAction(`¿Eliminar el código "${code.code}"?`)) {
      return;
    }
    this.actionSuccess.set('');
    this.api.delete(code.id).subscribe({
      next: () => {
        this.actionSuccess.set('Código eliminado.');
        scrollToTop();
        this.load();
      },
      error: (error) => {
        this.actionError.set(apiErrorMessage(error, 'No se pudo eliminar el código'));
        scrollToTop();
      },
    });
  }
}
