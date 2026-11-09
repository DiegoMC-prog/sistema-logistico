import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../core/ui/toast';
import { Brand } from '../../../shared/ui/brand';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Field } from '../../../shared/ui/field';

const coincide = (c: AbstractControl): ValidationErrors | null =>
  c.value === c.parent?.get('passwordNueva')?.value ? null : { mismatch: true };

/** Destino del enlace del correo: `/restablecer?token=…`. */
@Component({
  selector: 'app-restablecer-password-page',
  imports: [ReactiveFormsModule, RouterLink, Brand, Btn, Card, Field],
  template: `
    <main class="grid min-h-dvh place-items-center bg-canvas p-4">
      <div class="w-full max-w-sm">
        <div class="mb-8 flex justify-center"><app-brand /></div>
        <app-card class="p-8">
          <h1 class="text-xl font-bold tracking-tight text-ink">Elige una contraseña nueva</h1>

          @if (!token) {
            <p class="alert-error mt-6" role="alert">El enlace no es válido. Solicita uno nuevo para continuar.</p>
            <p class="mt-4 text-center text-sm">
              <a routerLink="/olvide-password" class="font-medium text-accent hover:underline">Solicitar un enlace nuevo</a>
            </p>
          } @else {
            <p class="mt-1 text-sm text-muted">Al guardarla se cerrarán tus sesiones abiertas y tendrás que iniciar sesión de nuevo.</p>
            <form [formGroup]="form" (ngSubmit)="guardar()" class="mt-6 space-y-5">
              @if (error()) {
                <p class="alert-error" role="alert">{{ error() }}</p>
                @if (enlaceInvalido()) {
                  <p class="text-center text-sm">
                    <a routerLink="/olvide-password" class="font-medium text-accent hover:underline">Solicitar un enlace nuevo</a>
                  </p>
                }
              }
              <app-field label="Contraseña nueva" [control]="form.controls.passwordNueva" helper="Entre 8 y 72 caracteres" required>
                <input type="password" class="field-input" formControlName="passwordNueva" autocomplete="new-password" />
              </app-field>
              <app-field label="Confirmar contraseña" [control]="form.controls.confirmacion" required>
                <input type="password" class="field-input" formControlName="confirmacion" autocomplete="new-password" />
              </app-field>
              <button appBtn type="submit" class="w-full" [disabled]="form.invalid || loading()">
                {{ loading() ? 'Guardando…' : 'Guardar contraseña' }}
              </button>
            </form>
          }
        </app-card>
      </div>
    </main>
  `,
})
export class RestablecerPasswordPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token');

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly enlaceInvalido = signal(false);

  protected readonly form = this.fb.group({
    passwordNueva: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    confirmacion: ['', [Validators.required, coincide]],
  });

  constructor() {
    // Si cambia la contraseña, hay que volver a validar la confirmación.
    this.form.controls.passwordNueva.valueChanges.subscribe(() => this.form.controls.confirmacion.updateValueAndValidity());
  }

  protected guardar(): void {
    if (this.form.invalid || !this.token || this.loading()) return;
    this.loading.set(true);
    this.error.set(null);
    this.enlaceInvalido.set(false);
    this.auth.restablecerPassword(this.token, this.form.getRawValue().passwordNueva).subscribe({
      next: () => {
        this.toast.success('Tu contraseña se actualizó. Te enviamos un correo de confirmación. Inicia sesión con la nueva contraseña.', {
          title: 'Contraseña actualizada',
          duration: 8000,
        });
        this.router.navigate(['/login']);
      },
      error: (e) => {
        this.loading.set(false);
        const msg = errorMessage(e, 'No se pudo restablecer la contraseña');
        this.error.set(msg);
        // Un 400 por enlace usado, vencido o reemplazado: ofrecer volver a pedirlo. Si solo falló la validación
        // de la contraseña, el enlace sigue sirviendo y se puede reintentar.
        this.enlaceInvalido.set(/enlace/i.test(msg));
      },
    });
  }
}
