import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { dirtyFormGuard } from './core/auth/can-deactivate.guard';
import { PublicLayoutComponent } from './layouts/public-layout/public-layout.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { AdminDashboardPageComponent } from './features/admin/dashboard/admin-dashboard-page.component';
import { AdminProductListPageComponent } from './features/admin/products/admin-product-list-page.component';
import { AdminProductFormPageComponent } from './features/admin/products/admin-product-form-page.component';
import { AdminCategoryListPageComponent } from './features/admin/categories/admin-category-list-page.component';
import { AdminCategoryFormPageComponent } from './features/admin/categories/admin-category-form-page.component';
import { AdminTechnicalServiceListPageComponent } from './features/admin/technical-services/admin-technical-service-list-page.component';
import { AdminTechnicalServiceFormPageComponent } from './features/admin/technical-services/admin-technical-service-form-page.component';
import { AdminInquiryListPageComponent } from './features/admin/inquiries/admin-inquiry-list-page.component';
import { AdminInquiryDetailPageComponent } from './features/admin/inquiries/admin-inquiry-detail-page.component';
import { AdminOrderListPageComponent } from './features/admin/orders/admin-order-list-page.component';
import { AdminOrderDetailPageComponent } from './features/admin/orders/admin-order-detail-page.component';
import { AdminCustomerListPageComponent } from './features/admin/customers/admin-customer-list-page.component';
import { AdminCustomerFormPageComponent } from './features/admin/customers/admin-customer-form-page.component';
import { AdminSettingsPageComponent } from './features/admin/settings/admin-settings-page.component';
import { AdminAuditPageComponent } from './features/admin/audit/admin-audit-page.component';
import { LoginPageComponent } from './features/auth/login/login-page.component';

const IPHONE_INTRO =
  'Catálogo de iPhone nuevos y usados en Uruguay. Filtrá por condición, modelo, capacidad y consultá disponibilidad por WhatsApp.';
const IPHONE_NEW_INTRO =
  'iPhone nuevos publicados en WA Shop. Revisá capacidad, color y precio, y consultá por WhatsApp antes de decidir.';
const IPHONE_USED_INTRO =
  'iPhone usados seleccionados, con salud de batería y garantía comercial indicada en cada ficha.';
const ACCESSORY_INTRO =
  'Accesorios para iPhone: fundas, cargadores y más. Stock y precios publicados en Uruguay.';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/public/home/home-page.component').then((m) => m.HomePageComponent),
      },
      {
        path: 'iphone',
        loadComponent: () =>
          import('./features/public/catalog/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: {
          productType: 'IPHONE',
          title: 'iPhone en Uruguay',
          seoKey: 'iphone',
          intro: IPHONE_INTRO,
        },
      },
      {
        path: 'iphone/nuevos',
        loadComponent: () =>
          import('./features/public/catalog/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: {
          productType: 'IPHONE',
          condition: 'NEW',
          title: 'iPhone nuevos en Uruguay',
          seoKey: 'iphone-nuevos',
          intro: IPHONE_NEW_INTRO,
        },
      },
      {
        path: 'iphone/usados',
        loadComponent: () =>
          import('./features/public/catalog/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: {
          productType: 'IPHONE',
          condition: 'USED',
          title: 'iPhone usados en Uruguay',
          seoKey: 'iphone-usados',
          intro: IPHONE_USED_INTRO,
        },
      },
      {
        path: 'iphone/modelo/:modelSlug',
        loadComponent: () =>
          import('./features/public/catalog/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: {
          productType: 'IPHONE',
          title: 'iPhone por modelo',
          seoKey: 'iphone-modelo',
          intro: 'Equipos filtrados por modelo. Compará opciones y consultá disponibilidad.',
        },
      },
      {
        path: 'iphone/:slug',
        loadComponent: () =>
          import('./features/public/product-detail/product-detail-page.component').then(
            (m) => m.ProductDetailPageComponent
          ),
      },
      {
        path: 'accesorios',
        loadComponent: () =>
          import('./features/public/catalog/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: {
          productType: 'ACCESSORY',
          title: 'Accesorios para iPhone',
          seoKey: 'accesorios',
          intro: ACCESSORY_INTRO,
        },
      },
      {
        path: 'accesorios/categoria/:categorySlug',
        loadComponent: () =>
          import('./features/public/catalog/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: {
          productType: 'ACCESSORY',
          title: 'Accesorios',
          seoKey: 'accesorios-categoria',
          intro: ACCESSORY_INTRO,
        },
      },
      {
        path: 'accesorios/:slug',
        loadComponent: () =>
          import('./features/public/product-detail/product-detail-page.component').then(
            (m) => m.ProductDetailPageComponent
          ),
      },
      {
        path: 'servicio-tecnico',
        loadComponent: () =>
          import('./features/public/technical-service/technical-service-page.component').then(
            (m) => m.TechnicalServicePageComponent
          ),
      },
      {
        path: 'servicio-tecnico/:slug',
        loadComponent: () =>
          import('./features/public/technical-service/technical-service-detail-page.component').then(
            (m) => m.TechnicalServiceDetailPageComponent
          ),
      },
      {
        path: 'contacto',
        loadComponent: () =>
          import('./features/public/contact/contact-page.component').then((m) => m.ContactPageComponent),
      },
      {
        path: 'preguntas-frecuentes',
        loadComponent: () =>
          import('./features/public/faq/faq-page.component').then((m) => m.FaqPageComponent),
      },
      {
        path: 'carrito',
        loadComponent: () =>
          import('./features/public/cart/cart-page.component').then((m) => m.CartPageComponent),
      },
      {
        path: 'checkout',
        loadComponent: () =>
          import('./features/public/checkout/checkout-page.component').then(
            (m) => m.CheckoutPageComponent
          ),
      },
      {
        path: 'checkout/exito',
        loadComponent: () =>
          import('./features/public/checkout/checkout-result-page.component').then(
            (m) => m.CheckoutResultPageComponent
          ),
        data: { kind: 'exito' },
      },
      {
        path: 'checkout/error',
        loadComponent: () =>
          import('./features/public/checkout/checkout-result-page.component').then(
            (m) => m.CheckoutResultPageComponent
          ),
        data: { kind: 'error' },
      },
      {
        path: 'checkout/pendiente',
        loadComponent: () =>
          import('./features/public/checkout/checkout-result-page.component').then(
            (m) => m.CheckoutResultPageComponent
          ),
        data: { kind: 'pendiente' },
      },
      {
        path: '404',
        loadComponent: () =>
          import('./features/public/not-found/not-found-page.component').then(
            (m) => m.NotFoundPageComponent
          ),
      },
      { path: 'catalogo', redirectTo: 'iphone', pathMatch: 'full' },
      { path: 'catalogo/:slug', redirectTo: 'iphone/:slug' },
    ],
  },
  {
    path: 'admin/login',
    component: AdminLayoutComponent,
    children: [{ path: '', component: LoginPageComponent }],
  },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: AdminDashboardPageComponent },
      {
        path: 'productos',
        component: AdminProductListPageComponent,
        data: { productType: 'IPHONE', title: 'Productos', basePath: '/admin/productos' },
      },
      {
        path: 'productos/nuevo',
        component: AdminProductFormPageComponent,
        canDeactivate: [dirtyFormGuard],
        data: { productType: 'IPHONE', title: 'Productos', basePath: '/admin/productos' },
      },
      {
        path: 'productos/:id',
        component: AdminProductFormPageComponent,
        canDeactivate: [dirtyFormGuard],
        data: { productType: 'IPHONE', title: 'Productos', basePath: '/admin/productos' },
      },
      {
        path: 'accesorios',
        component: AdminProductListPageComponent,
        data: { productType: 'ACCESSORY', title: 'Accesorios', basePath: '/admin/accesorios' },
      },
      {
        path: 'accesorios/nuevo',
        component: AdminProductFormPageComponent,
        canDeactivate: [dirtyFormGuard],
        data: { productType: 'ACCESSORY', title: 'Accesorios', basePath: '/admin/accesorios' },
      },
      {
        path: 'accesorios/:id',
        component: AdminProductFormPageComponent,
        canDeactivate: [dirtyFormGuard],
        data: { productType: 'ACCESSORY', title: 'Accesorios', basePath: '/admin/accesorios' },
      },
      { path: 'categorias', component: AdminCategoryListPageComponent },
      {
        path: 'categorias/nuevo',
        component: AdminCategoryFormPageComponent,
        canDeactivate: [dirtyFormGuard],
      },
      {
        path: 'categorias/:id',
        component: AdminCategoryFormPageComponent,
        canDeactivate: [dirtyFormGuard],
      },
      { path: 'servicios', component: AdminTechnicalServiceListPageComponent },
      {
        path: 'servicios/nuevo',
        component: AdminTechnicalServiceFormPageComponent,
        canDeactivate: [dirtyFormGuard],
      },
      {
        path: 'servicios/:id',
        component: AdminTechnicalServiceFormPageComponent,
        canDeactivate: [dirtyFormGuard],
      },
      { path: 'consultas', component: AdminInquiryListPageComponent },
      { path: 'consultas/:id', component: AdminInquiryDetailPageComponent },
      { path: 'pedidos', component: AdminOrderListPageComponent },
      { path: 'pedidos/:id', component: AdminOrderDetailPageComponent },
      { path: 'clientes', component: AdminCustomerListPageComponent },
      {
        path: 'clientes/nuevo',
        component: AdminCustomerFormPageComponent,
        canDeactivate: [dirtyFormGuard],
      },
      {
        path: 'clientes/:id',
        component: AdminCustomerFormPageComponent,
        canDeactivate: [dirtyFormGuard],
      },
      {
        path: 'configuracion',
        component: AdminSettingsPageComponent,
        canDeactivate: [dirtyFormGuard],
      },
      { path: 'auditoria', component: AdminAuditPageComponent },
    ],
  },
  {
    path: '**',
    component: PublicLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/public/not-found/not-found-page.component').then(
            (m) => m.NotFoundPageComponent
          ),
      },
    ],
  },
];
