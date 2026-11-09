import { Component, OnDestroy, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { AuthService } from '../../../core/auth/auth.service';
import { Brand } from '../../../shared/ui/brand';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Field } from '../../../shared/ui/field';

/** Segundos de espera antes de poder pedir otro enlace (el correo tarda unos segundos en llegar). */
const ESPERA_SEGUNDOS = 30;

@Component({
  selector: 'app-olvide-password-page',
  imports: [ReactiveFormsModule, RouterLink, Brand, Btn, Card, Field],
  template: `
    <main class="grid min-h-dvh place-items-center bg-canvas p-4">
      <div class="w-full max-w-sm">
        <div class="mb-8 flex justify-center"><app-brand /></div>
        <app-card class="p-8">
          <h1 class="text-xl font-bold tracking-tight text-ink">¿Olvidaste tu contraseña?</h1>
          <p class="mt-1 text-sm text-muted">Ingresa tu usuario o correo y te enviaremos un enlace para elegir una contraseña nueva.</p>

          <form [formGroup]="form" (ngSubmit)="enviar()" class="mt-6 space-y-5">
            @if (error()) {
              <p class="alert-error" role="alert">{{ error() }}</p>
            }
            @if (mensaje()) {
              <p class="alert-success" role="status">{{ mensaje() }}</p>
            }
            <app-field label="Usuario o correo" [control]="form.controls.identificador" required>
              <input class="field-input" formControlName="identificador" autocomplete="username" />
            </app-field>
            <button appBtn type="submit" class="w-full" [disabled]="form.invalid || loading() || espera() > 0">
              @if (loading()) { Enviando… }
              @else if (espera() > 0) { Reenviar en {{ espera() }} s }
              @else { {{ mensaje() ? 'Enviar otro enlace' : 'Enviar enlace' }} }
            </button>
            <p class="text-center text-sm">
              <a routerLink="/login" class="font-medium text-accent hover:underline">Volver a iniciar sesión</a>
            </p>
          </form>
        </app-card>
      </div>
    </main>
  `,
})
export class OlvidePasswordPage implements OnDestroy {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder).nonNullable;
  private timer?: ReturnType<typeof setInterval>;

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Mensaje del servidor, tal cual: responde igual exista o no la cuenta, y así debe mostrarse. */
  protected readonly mensaje = signal<string | null>(null);
  protected readonly espera = signal(0);
  protected readonly form = this.fb.group({ identificador: ['', Validators.required] });

  protected enviar(): void {
    if (this.form.invalid || this.loading() || this.espera() > 0) return;
    this.loading.set(true);
    this.error.set(null);
    this.auth.olvidePassword(this.form.getRawValue().identificador.trim()).subscribe({
      next: (r) => {
        this.loading.set(false);
        this.mensaje.set(r.mensaje);
        this.iniciarEspera();
      },
      error: (e) => {
        this.loading.set(false);
        this.mensaje.set(null);
        this.error.set(errorMessage(e, 'No se pudo enviar la solicitud')); // incluye el 429 con su detail
      },
    });
  }

  private iniciarEspera(): void {
    clearInterval(this.timer);
    this.espera.set(ESPERA_SEGUNDOS);
    this.timer = setInterval(() => {
      this.espera.update((n) => n - 1);
      if (this.espera() <= 0) clearInterval(this.timer);
    }, 1000);
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }
}
