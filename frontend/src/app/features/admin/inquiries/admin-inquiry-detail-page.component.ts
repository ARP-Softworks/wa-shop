import { DatePipe, NgClass } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminInquiryApiService } from '../../../core/api/admin-inquiry-api.service';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { UiSelectComponent } from '../../../shared/components/ui-select/ui-select.component';
import { AdminInquiryDetail, InquiryStatus } from '../../../shared/models/admin.models';
import { INQUIRY_STATUS_OPTIONS } from '../../../shared/models/inquiry-status-options';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { InquiryStatusLabelPipe } from '../../../shared/pipes/inquiry-status-label.pipe';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { scrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-admin-inquiry-detail-page',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    ReactiveFormsModule,
    RouterLink,
    AdminBreadcrumbsComponent,
    StatePanelComponent,
    InquiryStatusLabelPipe,
    UiSelectComponent,
  ],
  templateUrl: './admin-inquiry-detail-page.component.html',
  styleUrl: './admin-inquiry-detail-page.component.scss',
})
export class AdminInquiryDetailPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly inquiryApi = inject(AdminInquiryApiService);

  readonly state = signal<UiState<AdminInquiryDetail>>(loadingState());
  readonly submitError = signal('');
  readonly successMessage = signal('');
  readonly statusOptions = INQUIRY_STATUS_OPTIONS;
  readonly submitting = signal(false);

  readonly statusForm = this.fb.nonNullable.group({
    status: ['NEW' as InquiryStatus, Validators.required],
    note: [''],
    adminNotes: [''],
  });

  readonly whatsappUrl = computed(() => {
    const inquiry = this.state().data;
    if (!inquiry?.phone) {
      return null;
    }
    const digits = inquiry.phone.replace(/\D/g, '');
    if (!digits) {
      return null;
    }
    const text = inquiry.message?.trim() || `Hola ${inquiry.customerName}, te contactamos desde WA Shop.`;
    return `https://wa.me/${digits}?text=${encodeURIComponent(text)}`;
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.load(id);
    }
  }

  load(id: string): void {
    this.state.set(loadingState());
    this.inquiryApi.getById(id).subscribe({
      next: (inquiry) => {
        this.state.set(successState(inquiry));
        this.statusForm.reset({
          status: inquiry.status,
          note: '',
          adminNotes: inquiry.adminNotes ?? '',
        });
      },
      error: (error) => {
        this.state.set({
          status: 'error',
          message: apiErrorMessage(error, 'No se pudo cargar la consulta'),
        });
      },
    });
  }

  submitStatus(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.submitError.set('');
    this.successMessage.set('');
    this.statusForm.markAllAsTouched();

    if (this.statusForm.invalid) {
      return;
    }

    const raw = this.statusForm.getRawValue();
    this.submitting.set(true);

    this.inquiryApi
      .updateStatus(id, {
        status: raw.status,
        note: raw.note.trim() || null,
        adminNotes: raw.adminNotes.trim() || null,
      })
      .subscribe({
        next: (inquiry) => {
          this.submitting.set(false);
          this.state.set(successState(inquiry));
          this.statusForm.patchValue({ note: '' });
          this.successMessage.set('Estado actualizado correctamente.');
          scrollToTop();
        },
        error: (error) => {
          this.submitting.set(false);
          const parsed = parseApiError(error);
          this.submitError.set(apiErrorMessage(error, 'No se pudo actualizar el estado'));
          if (parsed?.details) {
            this.submitError.set(Object.values(mapFieldErrors(parsed.details)).join(' '));
          }
          scrollToTop();
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
