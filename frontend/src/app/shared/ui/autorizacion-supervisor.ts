import { Component, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AUTORIZACION_INVALIDA, AUTORIZACION_REQUERIDA, errorCode, errorMessage } from '../../core/api/api-error';

/** Credenciales del administrador que autoriza una operación sobre el monto umbral (R08). */
export interface Autorizacion {
  username: string;
  password: string;
}

/**
 * Estado del pedido de autorización de un formulario. El primer envío va sin credenciales; si el backend responde
 * AUTORIZACION_REQUERIDA se muestran los campos y el siguiente envío las incluye.
 */
export class AutorizacionEstado {
  /** Mensaje del backend cuando hace falta autorizar; null mientras no se pidió. */
  readonly requerida = signal<string | null>(null);
  username = '';
  password = '';

  /** Lo que va en el body: solo cuando el backend la pidió. */
  body(): Autorizacion | undefined {
    return this.requerida() ? { username: this.username.trim(), password: this.password } : undefined;
  }

  /**
   * Interpreta un error. Devuelve `manejado: true` si era de autorización; en ese caso `error` es lo que hay que
   * mostrar (null si solo hay que pedir las credenciales).
   */
  manejar(err: unknown): { manejado: boolean; error: string | null } {
    const code = errorCode(err);
    if (code === AUTORIZACION_REQUERIDA) {
      const yaPedida = this.requerida() !== null;
      this.requerida.set(errorMessage(err, 'Hace falta la autorización de un administrador'));
      return { manejado: true, error: yaPedida ? 'Ingrese el usuario y la contraseña del administrador' : null };
    }
    if (code === AUTORIZACION_INVALIDA) {
      this.password = '';
      return { manejado: true, error: errorMessage(err, 'Las credenciales del administrador no son válidas') };
    }
    return { manejado: false, error: null };
  }

  reset(): void {
    this.requerida.set(null);
    this.username = '';
    this.password = '';
  }
}

/** Recuadro con usuario y contraseña del administrador; solo se muestra cuando el backend pidió autorización. */
@Component({
  selector: 'app-autorizacion-supervisor',
  imports: [FormsModule],
  template: `
    @if (estado().requerida(); as mensaje) {
      <div class="space-y-3 rounded-lg border border-warning bg-warning-soft/40 p-4">
        <p class="text-sm font-semibold text-ink">{{ mensaje }}</p>
        <p class="text-xs text-muted">Un administrador debe ingresar sus credenciales. La autorización queda registrada.</p>
        <div class="grid gap-3 sm:grid-cols-2">
          <label class="flex flex-col gap-1">
            <span class="field-label">Usuario del administrador</span>
            <input class="field-input" autocomplete="off" [(ngModel)]="estado().username" name="autorizacion-usuario" />
          </label>
          <label class="flex flex-col gap-1">
            <span class="field-label">Contraseña</span>
            <input class="field-input" type="password" autocomplete="new-password" [(ngModel)]="estado().password" name="autorizacion-password" />
          </label>
        </div>
      </div>
    }
  `,
})
export class AutorizacionSupervisor {
  readonly estado = input.required<AutorizacionEstado>();
}
