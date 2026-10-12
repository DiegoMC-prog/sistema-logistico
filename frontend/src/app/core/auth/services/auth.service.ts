import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../../environments/environment.development';
import { AuthResponse, LoginRequest, UserSession } from '../models/auth.models';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';

  // 1. Estado reactivo con Signals
  private readonly _token = signal<string | null>(localStorage.getItem(this.TOKEN_KEY));
  private readonly _currentUser = signal<UserSession | null>(this.getStoredUser());

  // 2. Selectores públicos de solo lectura
  readonly token = this._token.asReadonly();
  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => !!this._token());
  readonly userRole = computed(() => this._currentUser()?.rol ?? null);

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((res) => {
        const userSession: UserSession = {
          id: res.id,
          username: res.username,
          email: res.email,
          rol: res.rol
        };

        localStorage.setItem(this.TOKEN_KEY, res.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(userSession));

        this._token.set(res.token);
        this._currentUser.set(userSession);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);

    this._token.set(null);
    this._currentUser.set(null);

    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this._token();
  }

  hasRole(role: string): boolean {
    return this._currentUser()?.rol === role;
  }

  private getStoredUser(): UserSession | null {
    const raw = localStorage.getItem(this.USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserSession;
    } catch {
      localStorage.removeItem(this.USER_KEY);
      return null;
    }
  }
}