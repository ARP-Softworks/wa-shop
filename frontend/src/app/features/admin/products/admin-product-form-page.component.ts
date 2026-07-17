import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Subscription } from 'rxjs';
import { AdminCategoryApiService } from '../../../core/api/admin-category-api.service';
import { AdminProductApiService } from '../../../core/api/admin-product-api.service';
import { CanComponentDeactivate } from '../../../core/auth/can-deactivate.guard';
import { AdminBreadcrumbsComponent } from '../../../shared/components/admin-breadcrumbs/admin-breadcrumbs.component';
import { AdminMediaUploaderComponent } from '../../../shared/components/admin-media-uploader/admin-media-uploader.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import {
  AdminCategory,
  AdminProductDetail,
  MediaUploadResponse,
  ProductImageWrite,
  ProductWriteRequest,
} from '../../../shared/models/admin.models';
import { CurrencyCode, ProductCondition, ProductType } from '../../../shared/models/catalog.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { slugify } from '../../../shared/utils/slugify.util';

interface ProductFormRouteData {
  productType?: ProductType;
  title?: string;
  basePath?: string;
}

function batteryRequiredValidator(control: AbstractControl): ValidationErrors | null {
  const parent = control.parent;
  if (!parent) {
    return null;
  }
  const productType = parent.get('productType')?.value as ProductType;
  const condition = parent.get('condition')?.value as ProductCondition;
  const value = control.value;

  if (productType === 'IPHONE' && condition === 'USED') {
    if (value === null || value === undefined || value === '') {
      return { required: true };
    }
    const num = Number(value);
    if (Number.isNaN(num) || num < 0 || num > 100) {
      return { range: true };
    }
  }
  return null;
}

@Component({
  selector: 'app-admin-product-form-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    AdminBreadcrumbsComponent,
    StatePanelComponent,
    AdminMediaUploaderComponent,
  ],
  templateUrl: './admin-product-form-page.component.html',
  styleUrl: './admin-product-form-page.component.scss',
})
export class AdminProductFormPageComponent implements OnInit, OnDestroy, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productApi = inject(AdminProductApiService);
  private readonly categoryApi = inject(AdminCategoryApiService);

  readonly pageTitle = signal('Producto');
  readonly basePath = signal('/admin/productos');
  readonly defaultProductType = signal<ProductType>('IPHONE');
  readonly isEdit = signal(false);
  readonly productId = signal<string | null>(null);
  readonly loadState = signal<UiState<AdminProductDetail>>(loadingState());
  readonly categories = signal<AdminCategory[]>([]);
  readonly submitError = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly successMessage = signal('');
  readonly submitting = signal(false);
  readonly imageUploadError = signal('');

  private slugManuallyEdited = false;
  private subscriptions = new Subscription();

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    slug: ['', [Validators.required, Validators.maxLength(220)]],
    model: ['', Validators.maxLength(120)],
    description: [''],
    productType: ['IPHONE' as ProductType, Validators.required],
    condition: ['NEW' as ProductCondition, Validators.required],
    storageCapacity: ['', Validators.maxLength(40)],
    color: ['', Validators.maxLength(80)],
    batteryHealth: this.fb.control<number | null>(null, [batteryRequiredValidator]),
    price: [0, [Validators.required, Validators.min(0)]],
    previousPrice: this.fb.control<number | null>(null, Validators.min(0)),
    currency: ['UYU' as CurrencyCode, Validators.required],
    stock: [0, [Validators.required, Validators.min(0)]],
    warranty: ['', Validators.maxLength(200)],
    imei: ['', Validators.maxLength(17)],
    published: [false],
    featured: [false],
    categoryId: [''],
    seoTitle: ['', Validators.maxLength(70)],
    metaDescription: ['', Validators.maxLength(320)],
    indexable: [true],
    features: this.fb.array([] as ReturnType<typeof this.createFeatureGroup>[]),
    images: this.fb.array([] as ReturnType<typeof this.createImageGroup>[]),
  });

  ngOnInit(): void {
    const data = this.route.snapshot.data as ProductFormRouteData;
    if (data.title) {
      this.pageTitle.set(data.title);
    }
    if (data.basePath) {
      this.basePath.set(data.basePath);
    }
    if (data.productType) {
      this.defaultProductType.set(data.productType);
      this.form.patchValue({ productType: data.productType });
    }

    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'nuevo') {
      this.isEdit.set(true);
      this.productId.set(id);
      this.loadProduct(id);
    } else {
      this.loadState.set(successState({} as AdminProductDetail));
    }

    this.loadCategories();

    this.subscriptions.add(
      this.form.controls.name.valueChanges.subscribe((name) => {
        if (!this.slugManuallyEdited) {
          this.form.controls.slug.setValue(slugify(name), { emitEvent: false });
        }
      })
    );

    this.subscriptions.add(
      this.form.controls.slug.valueChanges.subscribe(() => {
        this.slugManuallyEdited = true;
      })
    );

    this.subscriptions.add(
      this.form.controls.productType.valueChanges.subscribe(() => {
        this.form.controls.batteryHealth.updateValueAndValidity();
      })
    );

    this.subscriptions.add(
      this.form.controls.condition.valueChanges.subscribe(() => {
        this.form.controls.batteryHealth.updateValueAndValidity();
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  canDeactivate(): boolean {
    if (!this.form.dirty || this.submitting()) {
      return true;
    }
    return confirmAction('Hay cambios sin guardar. ¿Salir de todos modos?');
  }

  get features(): FormArray {
    return this.form.controls.features;
  }

  get images(): FormArray {
    return this.form.controls.images;
  }

  get serpTitle(): string {
    const seo = this.form.controls.seoTitle.value?.trim();
    return seo || this.form.controls.name.value?.trim() || 'Título del producto';
  }

  get serpDescription(): string {
    const meta = this.form.controls.metaDescription.value?.trim();
    if (meta) {
      return meta;
    }
    const desc = this.form.controls.description.value?.trim();
    return desc || 'Descripción que verán los buscadores…';
  }

  mediaFolder(): string {
    return this.defaultProductType() === 'ACCESSORY' ? 'accessories' : 'products';
  }

  addFeature(name = '', value = ''): void {
    this.features.push(this.createFeatureGroup(name, value));
  }

  removeFeature(index: number): void {
    this.features.removeAt(index);
    this.form.markAsDirty();
  }

  onImageUploaded(response: MediaUploadResponse): void {
    this.imageUploadError.set('');
    const isFirst = this.images.length === 0;
    this.images.push(
      this.createImageGroup({
        url: response.url,
        publicId: response.publicId,
        altText: '',
        format: response.format,
        sizeBytes: response.sizeBytes,
        width: response.width,
        height: response.height,
        mainImage: isFirst,
      })
    );
    this.form.markAsDirty();
  }

  onImageUploadFailed(message: string): void {
    this.imageUploadError.set(message);
  }

  removeImage(index: number): void {
    const wasMain = !!this.images.at(index).get('mainImage')?.value;
    this.images.removeAt(index);
    if (wasMain && this.images.length > 0) {
      this.setMainImage(0);
    }
    this.form.markAsDirty();
  }

  setMainImage(index: number): void {
    this.images.controls.forEach((control, i) => {
      control.get('mainImage')?.setValue(i === index);
    });
    this.form.markAsDirty();
  }

  moveImage(index: number, delta: number): void {
    const target = index + delta;
    if (target < 0 || target >= this.images.length) {
      return;
    }
    const current = this.images.at(index);
    this.images.removeAt(index);
    this.images.insert(target, current);
    this.form.markAsDirty();
  }

  submit(): void {
    this.submitError.set('');
    this.fieldErrors.set({});
    this.successMessage.set('');

    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.submitError.set('Revisá los campos marcados.');
      return;
    }

    const body = this.buildWriteRequest();
    this.submitting.set(true);

    const request$ = this.isEdit()
      ? this.productApi.update(this.productId()!, body)
      : this.productApi.create(body);

    request$.subscribe({
      next: (product) => {
        this.submitting.set(false);
        this.form.markAsPristine();
        this.successMessage.set('Producto guardado correctamente.');
        if (!this.isEdit()) {
          void this.router.navigate([this.basePath(), product.id]);
        } else {
          this.patchFormFromProduct(product);
        }
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo guardar el producto'));
        if (parsed?.details) {
          this.fieldErrors.set(mapFieldErrors(parsed.details));
        }
      },
    });
  }

  fieldError(field: string): string | null {
    const backend = this.fieldErrors()[field];
    if (backend) {
      return backend;
    }
    const control = this.form.get(field);
    if (!control?.touched || !control.invalid) {
      return null;
    }
    if (control.hasError('required')) {
      return 'Campo obligatorio';
    }
    if (control.hasError('min')) {
      return 'El valor debe ser mayor o igual a 0';
    }
    if (control.hasError('range')) {
      return 'Debe estar entre 0 y 100';
    }
    return 'Valor inválido';
  }

  private loadCategories(): void {
    this.categoryApi.list().subscribe({
      next: (items) => this.categories.set(items.filter((c) => c.active)),
      error: () => this.categories.set([]),
    });
  }

  private loadProduct(id: string): void {
    this.loadState.set(loadingState());
    this.productApi.getById(id).subscribe({
      next: (product) => {
        this.loadState.set(successState(product));
        this.patchFormFromProduct(product);
        this.slugManuallyEdited = true;
      },
      error: (error) => {
        this.loadState.set({
          status: 'error',
          message: apiErrorMessage(error, 'No se pudo cargar el producto'),
        });
      },
    });
  }

  private patchFormFromProduct(product: AdminProductDetail): void {
    while (this.features.length) {
      this.features.removeAt(0);
    }
    product.features.forEach((f) => this.addFeature(f.name, f.value));

    while (this.images.length) {
      this.images.removeAt(0);
    }
    [...product.images]
      .sort((a, b) => a.position - b.position)
      .forEach((img) =>
        this.images.push(
          this.createImageGroup({
            url: img.url,
            publicId: img.publicId,
            altText: img.altText ?? '',
            format: img.format ?? null,
            sizeBytes: img.sizeBytes ?? null,
            width: img.width ?? null,
            height: img.height ?? null,
            mainImage: img.mainImage,
          })
        )
      );

    this.form.patchValue({
      name: product.name,
      slug: product.slug,
      model: product.model ?? '',
      description: product.description ?? '',
      productType: product.productType,
      condition: product.condition,
      storageCapacity: product.storageCapacity ?? '',
      color: product.color ?? '',
      batteryHealth: product.batteryHealth,
      price: product.price,
      previousPrice: product.previousPrice,
      currency: product.currency,
      stock: product.stock,
      warranty: product.warranty ?? '',
      imei: product.imei ?? '',
      published: product.published,
      featured: product.featured,
      categoryId: product.categoryId ?? '',
      seoTitle: product.seoTitle ?? '',
      metaDescription: product.metaDescription ?? '',
      indexable: product.indexable ?? true,
    });
    this.form.markAsPristine();
  }

  private buildWriteRequest(): ProductWriteRequest {
    const raw = this.form.getRawValue();
    const images: ProductImageWrite[] = raw.images.map((item, index) => ({
      url: item.url.trim(),
      publicId: item.publicId.trim() || null,
      altText: item.altText.trim() || null,
      position: index,
      mainImage: !!item.mainImage,
      format: item.format || null,
      sizeBytes: item.sizeBytes,
      width: item.width,
      height: item.height,
    }));

    if (images.length > 0 && !images.some((img) => img.mainImage)) {
      images[0].mainImage = true;
    }

    return {
      name: raw.name.trim(),
      slug: raw.slug.trim(),
      model: raw.model.trim() || null,
      description: raw.description.trim() || null,
      productType: raw.productType,
      condition: raw.condition,
      storageCapacity: raw.storageCapacity.trim() || null,
      color: raw.color.trim() || null,
      batteryHealth: raw.batteryHealth,
      price: Number(raw.price),
      previousPrice: raw.previousPrice != null ? Number(raw.previousPrice) : null,
      currency: raw.currency,
      stock: Math.trunc(Number(raw.stock)),
      warranty: raw.warranty.trim() || null,
      imei: raw.imei.trim() || null,
      published: raw.published,
      featured: raw.featured,
      categoryId: raw.categoryId || null,
      seoTitle: raw.seoTitle.trim() || null,
      metaDescription: raw.metaDescription.trim() || null,
      indexable: raw.indexable,
      features: raw.features
        .filter((f) => f.name.trim() && f.value.trim())
        .map((f) => ({ name: f.name.trim(), value: f.value.trim() })),
      images,
    };
  }

  private createFeatureGroup(name = '', value = '') {
    return this.fb.nonNullable.group({
      name: [name, Validators.required],
      value: [value, Validators.required],
    });
  }

  private createImageGroup(data: {
    url?: string;
    publicId?: string | null;
    altText?: string;
    format?: string | null;
    sizeBytes?: number | null;
    width?: number | null;
    height?: number | null;
    mainImage?: boolean;
  } = {}) {
    return this.fb.nonNullable.group({
      url: [data.url ?? '', Validators.required],
      publicId: [data.publicId ?? ''],
      altText: [data.altText ?? '', Validators.maxLength(255)],
      format: [data.format ?? ''],
      sizeBytes: this.fb.control<number | null>(data.sizeBytes ?? null),
      width: this.fb.control<number | null>(data.width ?? null),
      height: this.fb.control<number | null>(data.height ?? null),
      mainImage: [!!data.mainImage],
    });
  }
}
