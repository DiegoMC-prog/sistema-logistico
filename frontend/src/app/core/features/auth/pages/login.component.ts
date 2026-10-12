import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/auth/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div style="max-width: 380px; margin: 4rem auto; font-family: sans-serif; padding: 2rem; border: 1px solid #e2e8f0; border-radius: 8px;">
      <h2 style="margin-bottom: 1.5rem; text-align: center;">Iniciar Sesión</h2>

      @if (errorMessage()) {
        <div style="background-color: #fee2e2; color: #dc2626; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem; font-size: 0.875rem;">
          {{ errorMessage() }}
        </div>
      }

      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <div style="margin-bottom: 1rem;">
          <label style="display: block; font-size: 0.875rem; font-weight: 600; margin-bottom: 0.25rem;">
            Usuario o Correo
          </label>
          <input
            type="text"
            formControlName="usernameOrEmail"
            placeholder="admin o admin@tienda.com"
            style="width: 100%; padding: 0.5rem; box-sizing: border-box; border: 1px solid #cbd5e1; border-radius: 4px;"
          />
        </div>

        <div style="margin-bottom: 1.5rem;">
          <label style="display: block; font-size: 0.875rem; font-weight: 600; margin-bottom: 0.25rem;">
            Contraseña
          </label>
          <input
            type="password"
            formControlName="password"
            placeholder="••••••••"
            style="width: 100%; padding: 0.5rem; box-sizing: border-box; border: 1px solid #cbd5e1; border-radius: 4px;"
          />
        </div>

        <button
          type="submit"
          [disabled]="form.invalid || loading()"
          style="width: 100%; padding: 0.65rem; background-color: #2563eb; color: #fff; border: none; border-radius: 4px; font-weight: 600; cursor: pointer;">
          {{ loading() ? 'Ingresando...' : 'Iniciar Sesión' }}
        </button>
      </form>
    </div>
  `
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    usernameOrEmail: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  onSubmit(): void {
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/categorias']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(
          err.error?.message ?? 'Credenciales incorrectas o error en el servidor.'
        );
      }
    });
  }
}