import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormGroup,
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
import { SuggestComboComponent } from '../../../shared/components/suggest-combo/suggest-combo.component';
import { StatePanelComponent } from '../../../shared/components/state-panel/state-panel.component';
import { UiSelectComponent } from '../../../shared/components/ui-select/ui-select.component';
import {
  AdminCategory,
  AdminProductDetail,
  AdminProductVariant,
  MediaUploadResponse,
  ProductImageWrite,
  ProductWriteRequest,
} from '../../../shared/models/admin.models';
import { CurrencyCode, ProductCondition, ProductType } from '../../../shared/models/catalog.models';
import { loadingState, successState, UiState } from '../../../shared/models/ui-state';
import { apiErrorMessage, mapFieldErrors, parseApiError } from '../../../shared/utils/api-error.util';
import { confirmAction } from '../../../shared/utils/confirm.util';
import { scrollToTop } from '../../../shared/utils/scroll.util';
import { slugify } from '../../../shared/utils/slugify.util';
import { IPHONE_MODELS, IPHONE_CAPACITIES, colorsForModel } from '../../../shared/data/iphone-catalog-reference';

interface ProductFormRouteData {
  productType?: ProductType;
  title?: string;
  basePath?: string;
}

function variantBatteryValidator(control: AbstractControl): ValidationErrors | null {
  const variant = control.parent;
  if (!variant) {
    return null;
  }
  const root = variant.parent?.parent;
  const productType = (root?.get('productType')?.value ?? 'IPHONE') as ProductType;
  const condition = variant.get('condition')?.value as ProductCondition;
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
    SuggestComboComponent,
    UiSelectComponent,
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
  readonly compatibleModels = signal<string[]>([]);
  readonly modelSearch = signal('');
  readonly modelPickerOpen = signal(false);
  readonly selectedVariantIndex = signal(0);

  readonly iphoneModels = IPHONE_MODELS;
  readonly iphoneCapacities = IPHONE_CAPACITIES;

  readonly productTypeOptions = [
    { value: 'IPHONE', label: 'iPhone' },
    { value: 'ACCESSORY', label: 'Accesorio' },
  ];

  readonly conditionOptions = [
    { value: 'NEW', label: 'Nuevo' },
    { value: 'USED', label: 'Usado' },
  ];

  readonly currencyOptions = [
    { value: 'UYU', label: 'UYU' },
    { value: 'USD', label: 'USD' },
  ];

  readonly categoryOptions = computed(() => [
    { value: '', label: 'Sin categoría' },
    ...this.categories().map((c) => ({ value: c.id, label: c.name })),
  ]);

  private slugManuallyEdited = false;
  private subscriptions = new Subscription();

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    slug: ['', [Validators.required, Validators.maxLength(220)]],
    model: ['', Validators.maxLength(120)],
    description: [''],
    productType: ['IPHONE' as ProductType, Validators.required],
    promoBuyQuantity: this.fb.control<number | null>(null, Validators.min(1)),
    promoPayQuantity: this.fb.control<number | null>(null, Validators.min(1)),
    published: [false],
    featured: [false],
    categoryId: [''],
    seoTitle: ['', Validators.maxLength(70)],
    metaDescription: ['', Validators.maxLength(320)],
    indexable: [true],
    features: this.fb.array([] as ReturnType<typeof this.createFeatureGroup>[]),
    variants: this.fb.array([] as ReturnType<typeof this.createVariantGroup>[]),
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
      this.addVariant();
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
        this.refreshVariantBatteryValidators();
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

  get variants(): FormArray {
    return this.form.controls.variants;
  }

  variantGroup(index: number): FormGroup {
    return this.variants.at(index) as FormGroup;
  }

  variantImages(index: number): FormArray {
    return this.variantGroup(index).get('images') as FormArray;
  }

  mediaFolder(): string {
    return this.defaultProductType() === 'ACCESSORY' ? 'accessories' : 'products';
  }

  colorOptionsForModel(): string[] {
    return colorsForModel(this.form.controls.model.value);
  }

  get modelSuggestions(): string[] {
    const term = this.modelSearch().trim().toLowerCase();
    const selected = this.compatibleModels();
    return this.iphoneModels.filter(
      (m) => !selected.includes(m) && (!term || m.toLowerCase().includes(term))
    );
  }

  onModelSearchInput(value: string): void {
    this.modelSearch.set(value);
    this.modelPickerOpen.set(true);
  }

  addCompatibleModel(model: string): void {
    if (!this.compatibleModels().includes(model)) {
      this.compatibleModels.set([...this.compatibleModels(), model]);
      this.form.markAsDirty();
    }
    this.modelSearch.set('');
  }

  removeCompatibleModel(model: string): void {
    this.compatibleModels.set(this.compatibleModels().filter((m) => m !== model));
    this.form.markAsDirty();
  }

  addFeature(name = '', value = ''): void {
    this.features.push(this.createFeatureGroup(name, value));
  }

  removeFeature(index: number): void {
    this.features.removeAt(index);
    this.form.markAsDirty();
  }

  addVariant(variant?: AdminProductVariant): void {
    this.variants.push(this.createVariantGroup(variant));
    this.selectedVariantIndex.set(this.variants.length - 1);
    this.form.markAsDirty();
  }

  removeVariant(index: number): void {
    if (this.variants.length <= 1) {
      this.submitError.set('El producto debe tener al menos una variante.');
      return;
    }
    this.variants.removeAt(index);
    this.selectedVariantIndex.set(Math.max(0, Math.min(index, this.variants.length - 1)));
    this.form.markAsDirty();
  }

  selectVariant(index: number): void {
    this.selectedVariantIndex.set(index);
  }

  onImageUploaded(response: MediaUploadResponse): void {
    this.imageUploadError.set('');
    const index = this.selectedVariantIndex();
    const images = this.variantImages(index);
    const isFirst = images.length === 0;
    images.push(
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

  removeImage(variantIndex: number, imageIndex: number): void {
    const images = this.variantImages(variantIndex);
    const wasMain = !!images.at(imageIndex).get('mainImage')?.value;
    images.removeAt(imageIndex);
    if (wasMain && images.length > 0) {
      this.setMainImage(variantIndex, 0);
    }
    this.form.markAsDirty();
  }

  setMainImage(variantIndex: number, imageIndex: number): void {
    this.variantImages(variantIndex).controls.forEach((control, i) => {
      control.get('mainImage')?.setValue(i === imageIndex);
    });
    this.form.markAsDirty();
  }

  moveImage(variantIndex: number, imageIndex: number, delta: number): void {
    const images = this.variantImages(variantIndex);
    const target = imageIndex + delta;
    if (target < 0 || target >= images.length) {
      return;
    }
    const current = images.at(imageIndex);
    images.removeAt(imageIndex);
    images.insert(target, current);
    this.form.markAsDirty();
  }

  submit(): void {
    this.submitError.set('');
    this.fieldErrors.set({});
    this.successMessage.set('');

    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.submitError.set('Revisá los campos marcados.');
      const firstInvalidVariant = this.variants.controls.findIndex((v) => v.invalid);
      if (firstInvalidVariant !== -1) {
        this.selectedVariantIndex.set(firstInvalidVariant);
      }
      scrollToTop();
      return;
    }
    if (this.variants.length === 0) {
      this.submitError.set('Agregá al menos una variante.');
      scrollToTop();
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
        scrollToTop();
      },
      error: (error) => {
        this.submitting.set(false);
        const parsed = parseApiError(error);
        this.submitError.set(apiErrorMessage(error, 'No se pudo guardar el producto'));
        if (parsed?.details) {
          this.fieldErrors.set(mapFieldErrors(parsed.details));
        }
        scrollToTop();
      },
    });
  }

  variantHasError(index: number): boolean {
    return this.variants.at(index).invalid;
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

  variantFieldError(variantIndex: number, field: string): string | null {
    const control = this.variantGroup(variantIndex).get(field);
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

    while (this.variants.length) {
      this.variants.removeAt(0);
    }
    const variants = product.variants?.length ? product.variants : [];
    if (variants.length === 0) {
      this.addVariant();
    } else {
      variants.forEach((variant) => this.variants.push(this.createVariantGroup(variant)));
    }
    this.selectedVariantIndex.set(0);

    this.form.patchValue({
      name: product.name,
      slug: product.slug,
      model: product.model ?? '',
      description: product.description ?? '',
      productType: product.productType,
      promoBuyQuantity: product.promoBuyQuantity,
      promoPayQuantity: product.promoPayQuantity,
      published: product.published,
      featured: product.featured,
      categoryId: product.categoryId ?? '',
      seoTitle: product.seoTitle ?? '',
      metaDescription: product.metaDescription ?? '',
      indexable: product.indexable ?? true,
    });
    this.compatibleModels.set(product.compatibleModels ?? []);
    this.refreshVariantBatteryValidators();
    this.form.markAsPristine();
  }

  private buildWriteRequest(): ProductWriteRequest {
    const raw = this.form.getRawValue();
    return {
      name: raw.name.trim(),
      slug: raw.slug.trim(),
      model: raw.model.trim() || null,
      description: raw.description.trim() || null,
      productType: raw.productType,
      promoBuyQuantity: raw.promoBuyQuantity != null ? Math.trunc(Number(raw.promoBuyQuantity)) : null,
      promoPayQuantity: raw.promoPayQuantity != null ? Math.trunc(Number(raw.promoPayQuantity)) : null,
      published: raw.published,
      featured: raw.featured,
      categoryId: raw.categoryId || null,
      seoTitle: raw.seoTitle.trim() || null,
      metaDescription: raw.metaDescription.trim() || null,
      indexable: raw.indexable,
      features: raw.features
        .filter((f) => f.name.trim() && f.value.trim())
        .map((f) => ({ name: f.name.trim(), value: f.value.trim() })),
      compatibleModels: raw.productType === 'ACCESSORY' ? this.compatibleModels() : [],
      variants: raw.variants.map((variant) => {
        const images: ProductImageWrite[] = variant.images.map((item, index) => ({
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
          id: variant.id || null,
          condition: variant.condition,
          storageCapacity: variant.storageCapacity.trim() || null,
          color: variant.color.trim() || null,
          batteryHealth: variant.batteryHealth,
          price: Number(variant.price),
          previousPrice: variant.previousPrice != null ? Number(variant.previousPrice) : null,
          currency: variant.currency,
          stock: Math.trunc(Number(variant.stock)),
          warranty: variant.warranty.trim() || null,
          imei: variant.imei.trim() || null,
          published: variant.published,
          images,
        };
      }),
    };
  }

  private refreshVariantBatteryValidators(): void {
    this.variants.controls.forEach((control) => {
      control.get('batteryHealth')?.updateValueAndValidity({ emitEvent: false });
    });
  }

  private createFeatureGroup(name = '', value = '') {
    return this.fb.nonNullable.group({
      name: [name, Validators.required],
      value: [value, Validators.required],
    });
  }

  private createVariantGroup(variant?: AdminProductVariant) {
    const group = this.fb.nonNullable.group({
      id: [variant?.id ?? ''],
      condition: [(variant?.condition ?? 'NEW') as ProductCondition, Validators.required],
      storageCapacity: [variant?.storageCapacity ?? '', Validators.maxLength(40)],
      color: [variant?.color ?? '', Validators.maxLength(80)],
      batteryHealth: this.fb.control<number | null>(variant?.batteryHealth ?? null, [variantBatteryValidator]),
      price: [variant?.price ?? 0, [Validators.required, Validators.min(0)]],
      previousPrice: this.fb.control<number | null>(variant?.previousPrice ?? null, Validators.min(0)),
      currency: [(variant?.currency ?? 'UYU') as CurrencyCode, Validators.required],
      stock: [variant?.stock ?? 0, [Validators.required, Validators.min(0)]],
      warranty: [variant?.warranty ?? '', Validators.maxLength(200)],
      imei: [variant?.imei ?? '', Validators.maxLength(17)],
      published: [variant?.published ?? true],
      images: this.fb.array([] as ReturnType<typeof this.createImageGroup>[]),
    });

    group.controls.condition.valueChanges.subscribe(() => {
      group.controls.batteryHealth.updateValueAndValidity();
    });

    const images = group.controls.images;
    [...(variant?.images ?? [])]
      .sort((a, b) => a.position - b.position)
      .forEach((img) =>
        images.push(
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

    return group;
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
