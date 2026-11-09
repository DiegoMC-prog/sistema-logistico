import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { Brand } from '../../shared/ui/brand';
import { IconBtn } from '../../shared/ui/button';
import { ConfirmDialog } from '../../shared/ui/confirm-dialog';
import { errorMessage } from '../api/api-error';
import { Icon } from '../../shared/ui/icon';
import { AuthService } from '../auth/auth.service';
import { NAV_SECTIONS } from './nav';
import { ThemeService } from './theme.service';

const COLLAPSED_KEY = 'nav.collapsed';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Brand, ConfirmDialog, Icon, IconBtn],
  template: `
    <div class="min-h-screen bg-canvas font-sans text-ink">
      @if (menuOpen()) {
        <button
          type="button"
          aria-label="Cerrar navegación"
          class="fixed inset-0 z-20 animate-fade-in bg-overlay lg:hidden"
          (click)="menuOpen.set(false)"
        ></button>
      }

      <aside
        class="fixed inset-y-0 left-0 z-30 flex w-sidebar flex-col bg-brand px-4 py-6 text-white transition-transform lg:translate-x-0"
        [class]="menuOpen() ? 'translate-x-0' : '-translate-x-full'"
      >
        <div class="px-2"><app-brand [onDark]="true" /></div>

        <nav class="sidebar-scroll mt-8 min-h-0 flex-1 space-y-5 overflow-y-auto pb-4" aria-label="Principal">
          @for (section of nav(); track section.title) {
            <div>
              <button
                type="button"
                class="mb-1 flex w-full items-center justify-between rounded-lg px-3 py-1.5 text-xs font-semibold uppercase tracking-wider text-sidebar-muted transition hover:bg-white/5 hover:text-white"
                [attr.aria-expanded]="!isCollapsed(section.title)"
                (click)="toggleSection(section.title)"
              >
                {{ section.title }}
                <app-icon
                  name="chevron"
                  class="size-4 transition-transform"
                  [class]="isCollapsed(section.title) ? '' : 'rotate-90'"
                />
              </button>
              @if (!isCollapsed(section.title)) {
                <div class="flex flex-col gap-0.5">
                  @for (item of section.items; track item.path) {
                    <a
                      [routerLink]="item.path"
                      routerLinkActive="bg-white/10 !text-white"
                      class="flex h-10 items-center gap-3 rounded-lg px-3 text-sm font-medium text-sidebar-muted transition hover:bg-white/5 hover:text-white"
                      (click)="menuOpen.set(false)"
                    >
                      <app-icon [name]="item.icon" />{{ item.label }}
                    </a>
                  }
                </div>
              }
            </div>
          }
        </nav>

        <div class="shrink-0 rounded-xl border border-white/10 bg-white/5 p-4">
          <div class="flex items-center gap-3">
            <div class="grid size-9 shrink-0 place-items-center rounded-full bg-accent text-sm font-semibold">
              {{ initials() }}
            </div>
            <div class="min-w-0">
              <p class="truncate text-sm font-semibold">{{ auth.perfil()?.nombre }}</p>
              <p class="truncate text-xs text-sidebar-muted">{{ auth.perfil()?.roles?.[0] ?? '' }}</p>
            </div>
          </div>
        </div>
      </aside>

      <div class="min-h-screen lg:pl-sidebar">
        <header
          class="sticky top-0 z-10 flex min-h-header items-center justify-between border-b border-border bg-canvas/90 px-5 backdrop-blur md:px-8"
        >
          <div class="flex items-center gap-3">
            <button appIconBtn icon="menu" label="Abrir navegación" class="lg:hidden" (click)="menuOpen.set(true)"></button>
            <div>
              <h1 class="text-xl font-bold tracking-tight text-ink md:text-2xl">{{ page().title }}</h1>
              @if (page().subtitle) {
                <p class="hidden text-sm text-muted sm:block">{{ page().subtitle }}</p>
              }
            </div>
          </div>
          <div class="flex items-center gap-1 md:gap-2">
            <button
              appIconBtn
              [icon]="theme.dark() ? 'sun' : 'moon'"
              label="Cambiar tema"
              (click)="theme.toggle()"
            ></button>
            <div class="mx-2 hidden h-7 w-px bg-border sm:block"></div>
            <div class="hidden items-center gap-2 p-1.5 md:flex">
              <div class="grid size-8 place-items-center rounded-full bg-brand text-xs font-bold text-white">
                {{ initials() }}
              </div>
              <span class="text-sm font-semibold text-ink">{{ auth.perfil()?.nombre }}</span>
            </div>
            <button appIconBtn icon="shield" label="Cerrar sesión en todos los dispositivos" (click)="logoutAllOpen.set(true)"></button>
            <button appIconBtn icon="logout" label="Cerrar sesión" (click)="auth.logout()"></button>
          </div>
        </header>

        <main class="page-enter p-5 md:p-8"><router-outlet /></main>
      </div>
    </div>

    @if (logoutAllOpen()) {
      <app-confirm-dialog
        title="¿Cerrar sesión en todos los dispositivos?"
        message="Se cerrará tu sesión en este y en cualquier otro dispositivo donde hayas iniciado sesión."
        confirmLabel="Cerrar todas las sesiones"
        icon="logout"
        [busy]="loggingOut()"
        [error]="logoutError()"
        (confirmed)="logoutAll()"
        (cancelled)="logoutAllOpen.set(false)"
      />
    }
  `,
})
export class Shell {
  protected readonly auth = inject(AuthService);
  protected readonly theme = inject(ThemeService);
  private readonly router = inject(Router);

  protected readonly menuOpen = signal(false);
  protected readonly logoutAllOpen = signal(false);
  protected readonly loggingOut = signal(false);
  protected readonly logoutError = signal<string | null>(null);

  /** Solo los ítems permitidos para el usuario; las secciones que quedan vacías se omiten. */
  protected readonly nav = computed(() =>
    NAV_SECTIONS.map((s) => ({ title: s.title, items: s.items.filter((i) => this.auth.hasAnyPermission(i.permiso)) })).filter(
      (s) => s.items.length > 0,
    ),
  );

  /** Secciones plegadas por el usuario; se recuerdan entre visitas. */
  private readonly collapsed = signal<ReadonlySet<string>>(this.readCollapsed());

  protected isCollapsed(title: string): boolean {
    return this.collapsed().has(title);
  }

  protected toggleSection(title: string): void {
    const next = new Set(this.collapsed());
    next.has(title) ? next.delete(title) : next.add(title);
    this.setCollapsed(next);
  }

  constructor() {
    // Al navegar, la sección de la página actual se despliega para no ocultar el ítem activo.
    this.router.events.pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd)).subscribe((e) => {
      const url = e.urlAfterRedirects;
      const activa = NAV_SECTIONS.find((s) => s.items.some((i) => url === i.path || url.startsWith(i.path + '/') || url.startsWith(i.path + '?')));
      if (activa && this.collapsed().has(activa.title)) {
        const next = new Set(this.collapsed());
        next.delete(activa.title);
        this.setCollapsed(next);
      }
    });
  }

  private setCollapsed(next: Set<string>): void {
    this.collapsed.set(next);
    try {
      localStorage.setItem(COLLAPSED_KEY, JSON.stringify([...next]));
    } catch {
      /* almacenamiento no disponible: el estado vive solo en memoria */
    }
  }

  private readCollapsed(): ReadonlySet<string> {
    try {
      const raw = JSON.parse(localStorage.getItem(COLLAPSED_KEY) ?? '[]');
      return new Set(Array.isArray(raw) ? raw.filter((x): x is string => typeof x === 'string') : []);
    } catch {
      return new Set();
    }
  }

  protected logoutAll(): void {
    this.loggingOut.set(true);
    this.logoutError.set(null);
    this.auth.logoutTodas().subscribe({
      error: (e) => {
        this.loggingOut.set(false);
        this.logoutError.set(errorMessage(e, 'No se pudo cerrar las sesiones'));
      },
    });
  }

  /** Título y subtítulo salen de `data` de la ruta activa más profunda. */
  protected readonly page = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      startWith(null),
      map(() => {
        let r = this.router.routerState.snapshot.root;
        while (r.firstChild) r = r.firstChild;
        const d = r.data ?? {};
        return { title: (d['title'] as string) ?? '', subtitle: (d['subtitle'] as string) ?? '' };
      }),
    ),
    { initialValue: { title: '', subtitle: '' } },
  );

  protected readonly initials = computed(() =>
    (this.auth.perfil()?.nombre ?? '?')
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join(''),
  );
}
