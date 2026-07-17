import { Component, EventEmitter, Input, OnDestroy, Output, inject, signal } from '@angular/core';
import { AdminMediaApiService } from '../../../core/api/admin-media-api.service';
import { MediaUploadResponse } from '../../models/admin.models';
import { apiErrorMessage } from '../../utils/api-error.util';

@Component({
  selector: 'app-admin-media-uploader',
  standalone: true,
  templateUrl: './admin-media-uploader.component.html',
  styleUrl: './admin-media-uploader.component.scss',
})
export class AdminMediaUploaderComponent implements OnDestroy {
  private readonly mediaApi = inject(AdminMediaApiService);

  @Input() folder = 'uploads';
  @Input() label = 'Subir imagen';
  @Input() previewUrl: string | null = null;
  @Input() disabled = false;

  @Output() readonly uploaded = new EventEmitter<MediaUploadResponse>();
  @Output() readonly failed = new EventEmitter<string>();

  readonly progress = signal(0);
  readonly uploading = signal(false);
  readonly error = signal('');
  readonly localPreview = signal<string | null>(null);

  private objectUrl: string | null = null;
  private pendingFile: File | null = null;

  ngOnDestroy(): void {
    this.revokePreview();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file || this.disabled) {
      return;
    }
    this.startUpload(file);
  }

  retry(): void {
    if (this.pendingFile && !this.uploading()) {
      this.startUpload(this.pendingFile);
    }
  }

  clearError(): void {
    this.error.set('');
  }

  private startUpload(file: File): void {
    this.pendingFile = file;
    this.error.set('');
    this.uploading.set(true);
    this.progress.set(0);
    this.setLocalPreview(file);

    this.mediaApi.upload(file, this.folder).subscribe({
      next: (event) => {
        this.progress.set(event.progress);
        if (event.response) {
          this.uploading.set(false);
          this.pendingFile = null;
          this.uploaded.emit(event.response);
        }
      },
      error: (err) => {
        this.uploading.set(false);
        const message = apiErrorMessage(err, 'No se pudo subir la imagen');
        this.error.set(message);
        this.failed.emit(message);
        // Keep form/parent data intact; only this slot shows the error.
      },
    });
  }

  private setLocalPreview(file: File): void {
    this.revokePreview();
    this.objectUrl = URL.createObjectURL(file);
    this.localPreview.set(this.objectUrl);
  }

  private revokePreview(): void {
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }
  }

  displayPreview(): string | null {
    return this.localPreview() || this.previewUrl;
  }
}
