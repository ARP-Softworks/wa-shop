import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

interface AdminNavItem {
  label: string;
  link: string;
  icon: string;
  exact?: boolean;
}

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.scss',
})
export class AdminLayoutComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly sidebarOpen = signal(false);

  readonly navItems: AdminNavItem[] = [
    { label: 'Panel', link: '/admin', icon: 'dashboard', exact: true },
    { label: 'Productos', link: '/admin/productos', icon: 'products' },
    { label: 'Categorías', link: '/admin/categorias', icon: 'categories' },
    { label: 'Accesorios', link: '/admin/accesorios', icon: 'accessories' },
    { label: 'Servicios', link: '/admin/servicios', icon: 'services' },
    { label: 'Consultas', link: '/admin/consultas', icon: 'inquiries' },
    { label: 'Banners', link: '/admin/banners', icon: 'banners' },
    { label: 'Pedidos', link: '/admin/pedidos', icon: 'orders' },
    { label: 'Promociones', link: '/admin/promociones', icon: 'promotions' },
    { label: 'Códigos descuento', link: '/admin/codigos-descuento', icon: 'promotions' },
    { label: 'Clientes', link: '/admin/clientes', icon: 'customers' },
    { label: 'Usuarios', link: '/admin/usuarios', icon: 'users' },
    { label: 'Configuración', link: '/admin/configuracion', icon: 'settings' },
    { label: 'Auditoría', link: '/admin/auditoria', icon: 'audit' },
  ];

  readonly userInitials = computed(() => {
    const user = this.auth.user();
    if (!user) {
      return '';
    }
    return `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase() || 'A';
  });

  toggleSidebar(): void {
    this.sidebarOpen.update((open) => !open);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  logout(): void {
    this.auth.logout().subscribe({
      next: () => void this.router.navigate(['/admin/login']),
      error: () => void this.router.navigate(['/admin/login']),
    });
  }
}
