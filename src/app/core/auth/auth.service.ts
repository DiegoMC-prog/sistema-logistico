import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, of, shareReplay, switchMap, tap, throwError } from 'rxjs';
import { API_URL } from '../api/api-url';
import { AccionesCrud, PermisosCrud } from './permisos';
import { CambiarPasswordRequest, LoginRequest, Perfil, TokenResponse } from './auth.models';

const ACCESS_KEY = 'auth.access';
const REFRESH_KEY = 'auth.refresh';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _accessToken = signal<string | null>(this.read(ACCESS_KEY));
  private readonly _refreshToken = signal<string | null>(this.read(REFRESH_KEY));
  private readonly _perfil = signal<Perfil | null>(null);
  private refreshInFlight$: Observable<TokenResponse> | null = null;

  readonly perfil = this._perfil.asReadonly();
  readonly accessToken = this._accessToken.asReadonly();
  readonly isAuthenticated = computed(() => !!this._accessToken());
  readonly passwordTemporal = computed(() => this._perfil()?.passwordTemporal ?? false);

  login(req: LoginRequest): Observable<Perfil> {
    return this.http.post<TokenResponse>(`${API_URL}/auth/login`, req).pipe(
      tap((t) => this.saveTokens(t)),
      switchMap(() => this.loadPerfil()),
    );
  }

  /** Carga el perfil desde el backend (también tras recargar la página). */
  loadPerfil(): Observable<Perfil> {
    return this.http.get<Perfil>(`${API_URL}/auth/me`).pipe(tap((p) => this._perfil.set(p)));
  }

  /** Renueva tokens; las llamadas simultáneas comparten una sola petición. */
  refresh(): Observable<TokenResponse> {
    const refreshToken = this._refreshToken();
    if (!refreshToken) return throwError(() => new Error('Sin refresh token'));

    this.refreshInFlight$ ??= this.http
      .post<TokenResponse>(`${API_URL}/auth/refresh`, { refreshToken })
      .pipe(
        tap((t) => this.saveTokens(t)),
        finalize(() => (this.refreshInFlight$ = null)),
        shareReplay(1),
      );
    return this.refreshInFlight$;
  }

  cambiarPassword(req: CambiarPasswordRequest): Observable<Perfil> {
    return this.http
      .post<void>(`${API_URL}/auth/cambiar-password`, req)
      .pipe(switchMap(() => this.loadPerfil()));
  }

  /** Pide el enlace de recuperación. Responde 202 siempre igual, exista o no la cuenta. */
  olvidePassword(identificador: string): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(`${API_URL}/auth/olvide-password`, { identificador });
  }

  /** Fija la contraseña nueva con el token del correo (204). Cierra todas las sesiones del usuario. */
  restablecerPassword(token: string, passwordNueva: string): Observable<void> {
    return this.http.post<void>(`${API_URL}/auth/restablecer-password`, { token, passwordNueva }).pipe(
      tap(() => this.clear()), // las sesiones de este navegador ya no sirven
    );
  }

  logout(): void {
    const refreshToken = this._refreshToken();
    const done = () => {
      this.clear();
      this.router.navigate(['/login']);
    };
    if (!refreshToken) return done();
    this.http
      .post<void>(`${API_URL}/auth/logout`, { refreshToken })
      .pipe(catchError(() => of(null)))
      .subscribe(done);
  }

  /** Invalida los refresh tokens de todos los dispositivos y cierra esta sesión. */
  logoutTodas(): Observable<void> {
    return this.http.post<void>(`${API_URL}/auth/logout-todas`, null).pipe(
      tap(() => {
        this.clear();
        this.router.navigate(['/login']);
      }),
    );
  }

  /** Limpia la sesión local sin llamar al backend. */
  clear(): void {
    this._accessToken.set(null);
    this._refreshToken.set(null);
    this._perfil.set(null);
    this.write(ACCESS_KEY, null);
    this.write(REFRESH_KEY, null);
  }

  hasPermission(permiso: string): boolean {
    return this._perfil()?.permisos.includes(permiso) ?? false;
  }

  /** Acciones permitidas sobre un módulo de CRUD: `protected readonly puede = inject(AuthService).crud(MODULOS.catalogo)`. */
  crud(m: PermisosCrud): AccionesCrud {
    return {
      crear: this.hasPermission(m.crear),
      editar: this.hasPermission(m.editar),
      eliminar: this.hasPermission(m.eliminar),
      activar: this.hasPermission(m.activar),
    };
  }

  hasAnyPermission(permisos: string | readonly string[]): boolean {
    const list = typeof permisos === 'string' ? [permisos] : permisos;
    return list.some((p) => this.hasPermission(p));
  }

  private saveTokens(t: TokenResponse): void {
    this._accessToken.set(t.accessToken);
    this._refreshToken.set(t.refreshToken);
    this.write(ACCESS_KEY, t.accessToken);
    this.write(REFRESH_KEY, t.refreshToken);
  }

  private read(key: string): string | null {
    try {
      return localStorage.getItem(key);
    } catch {
      return null;
    }
  }

  private write(key: string, value: string | null): void {
    try {
      if (value === null) localStorage.removeItem(key);
      else localStorage.setItem(key, value);
    } catch {
      /* almacenamiento no disponible: la sesión vive solo en memoria */
    }
  }
}
