import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { AuthService } from '../../../core/auth/auth.service';
import { Brand } from '../../../shared/ui/brand';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Field } from '../../../shared/ui/field';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink, Brand, Btn, Card, Field],
  template: `
    <main class="grid min-h-dvh place-items-center bg-canvas p-4">
      <div class="w-full max-w-sm">
        <div class="mb-8 flex justify-center"><app-brand /></div>
        <app-card class="p-8">
          <h1 class="text-xl font-bold tracking-tight text-ink">Iniciar sesión</h1>
          <p class="mt-1 text-sm text-muted">Ingresa tus credenciales para continuar.</p>

          <form [formGroup]="form" (ngSubmit)="submit()" class="mt-6 space-y-5">
            @if (error()) {
              <p class="alert-error" role="alert">{{ error() }}</p>
            }
            <app-field label="Usuario" [control]="form.controls.username" required>
              <input class="field-input" formControlName="username" autocomplete="username" />
            </app-field>
            <app-field label="Contraseña" [control]="form.controls.password" required>
              <input type="password" class="field-input" formControlName="password" autocomplete="current-password" />
            </app-field>
            <button appBtn type="submit" class="w-full" [disabled]="form.invalid || loading()">
              {{ loading() ? 'Ingresando…' : 'Ingresar' }}
            </button>
            <p class="text-center text-sm">
              <a routerLink="/olvide-password" class="font-medium text-accent hover:underline">¿Olvidaste tu contraseña?</a>
            </p>
          </form>
        </app-card>
      </div>
    </main>
  `,
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly form = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (perfil) => this.router.navigate([perfil.passwordTemporal ? '/cambiar-password' : '/']),
      error: (e) => {
        this.loading.set(false);
        this.error.set(
          e?.status === 401 || e?.status === 400 ? 'Usuario o contraseña incorrectos' : errorMessage(e),
        );
      },
    });
  }
}
