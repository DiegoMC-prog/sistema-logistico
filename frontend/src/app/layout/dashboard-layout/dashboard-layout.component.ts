import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { IconComponent, IconName } from '../../shared/components/icon/icon.component';
import { AuthService } from '../../core/auth/services/auth.service';
import { ToastContainerComponent } from '../../shared/components/toast/toast-container.component';

interface NavItem {
  label: string;
  route: string;
  icon: IconName;
  badge?: string;
}

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, IconComponent, ToastContainerComponent],
  templateUrl: './dashboard-layout.component.html',
})
export class DashboardLayoutComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  menuOpen = signal<boolean>(false);
  isDarkMode = signal<boolean>(false);
  userMenuOpen = signal<boolean>(false);

  // Datos de usuario desde AuthService o fallbacks
  currentUser = this.authService.currentUser;
  userName = computed(() => this.currentUser()?.username ?? 'Alex Morgan');
  userRole = computed(() => this.currentUser()?.rol ?? 'Warehouse Manager');
  userInitials = computed(() => {
    const name = this.userName().trim();
    if (!name) return 'WM';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  });

  // Título dinámico según la ruta activa
  currentTitle = signal<string>('Gestión de Categorías');
  currentSubtitle = signal<string>('Organiza y administra la taxonomía de productos');

  workspaceNav: NavItem[] = [
    { label: 'Overview', route: '/dashboard', icon: 'grid' },
    { label: 'Inventory', route: '/inventario', icon: 'box', badge: '1,284' },
  ];

  manageNav: NavItem[] = [
    { label: 'Categorias', route: '/categorias', icon: 'clipboard' },
    { label: 'Depositos', route: '/depositos', icon: 'location' },
  ];

  ngOnInit(): void {
    // Inicializar modo oscuro desde preferencias o localStorage
    const savedTheme = localStorage.getItem('theme');
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    if (savedTheme === 'dark' || (!savedTheme && prefersDark)) {
      this.isDarkMode.set(true);
      document.documentElement.classList.add('dark');
    }

    this.actualizarTitulos(this.router.url);

    // Escuchar cambios de ruta para actualizar títulos y cerrar menú móvil
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.actualizarTitulos(event.urlAfterRedirects);
        this.menuOpen.set(false);
        this.userMenuOpen.set(false);
      });
  }

  toggleDarkMode(): void {
    this.isDarkMode.update((dark) => !dark);
    if (this.isDarkMode()) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  toggleUserMenu(): void {
    this.userMenuOpen.update((open) => !open);
  }

  logout(): void {
    this.userMenuOpen.set(false);
    this.authService.logout();
  }

  private actualizarTitulos(url: string): void {
    if (url.includes('/categorias')) {
      this.currentTitle.set('Categorías');
      this.currentSubtitle.set('Organiza y administra la taxonomía de productos');
    } else if (url.includes('/depositos') || url.includes('/warehouses')) {
      this.currentTitle.set('Depósitos');
      this.currentSubtitle.set('Administra instalaciones de almacenamiento y su disponibilidad operativa');
    } else if (url.includes('/inventario') || url.includes('/inventory')) {
      this.currentTitle.set('Inventory');
      this.currentSubtitle.set('1,284 products across 4 warehouses');
    } else if (url.includes('/dashboard') || url.includes('/overview')) {
      this.currentTitle.set('Dashboard');
      this.currentSubtitle.set('Here’s what’s happening across your warehouse today');
    } else {
      this.currentTitle.set('Warehouse OS');
      this.currentSubtitle.set('Sistema de gestión logística y almacenes');
    }
  }
}
