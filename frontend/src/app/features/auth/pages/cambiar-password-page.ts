import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { AuthService } from '../../../core/auth/auth.service';
import { Brand } from '../../../shared/ui/brand';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Field } from '../../../shared/ui/field';

@Component({
  selector: 'app-cambiar-password-page',
  imports: [ReactiveFormsModule, Brand, Btn, Card, Field],
  template: `
    <main class="grid min-h-dvh place-items-center bg-canvas p-4">
      <div class="w-full max-w-sm">
        <div class="mb-8 flex justify-center"><app-brand /></div>
        <app-card class="p-8">
          <h1 class="text-xl font-bold tracking-tight text-ink">Cambiar contraseña</h1>
          <p class="mt-1 text-sm text-muted">
            {{ auth.passwordTemporal() ? 'Tu contraseña es temporal. Debes cambiarla para continuar.' : 'Elige una contraseña nueva.' }}
          </p>

          <form [formGroup]="form" (ngSubmit)="submit()" class="mt-6 space-y-5">
            @if (error()) {
              <p class="alert-error" role="alert">{{ error() }}</p>
            }
            <app-field label="Contraseña actual" [control]="form.controls.passwordActual" required>
              <input type="password" class="field-input" formControlName="passwordActual" autocomplete="current-password" />
            </app-field>
            <app-field label="Nueva contraseña" [control]="form.controls.passwordNueva" helper="Mínimo 8 caracteres" required>
              <input type="password" class="field-input" formControlName="passwordNueva" autocomplete="new-password" />
            </app-field>
            <button appBtn type="submit" class="w-full" [disabled]="form.invalid || loading()">
              {{ loading() ? 'Guardando…' : 'Guardar contraseña' }}
            </button>
          </form>
        </app-card>
      </div>
    </main>
  `,
})
export class CambiarPasswordPage {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly form = this.fb.group({
    passwordActual: ['', Validators.required],
    passwordNueva: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set(null);
    this.auth.cambiarPassword(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/']),
      error: (e) => {
        this.loading.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
