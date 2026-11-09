import { Component } from '@angular/core';
import { Icon } from '../../../shared/ui/icon';

@Component({
  selector: 'app-sin-acceso-page',
  imports: [Icon],
  template: `
    <div class="mx-auto flex max-w-md flex-col items-center py-20 text-center">
      <div class="grid size-14 place-items-center rounded-full bg-warning-soft text-warning-dark">
        <app-icon name="shield" class="size-7" />
      </div>
      <h2 class="mt-5 text-lg font-bold text-ink">Sin acceso a ninguna sección</h2>
      <p class="mt-2 text-sm leading-6 text-muted">
        Tu cuenta no tiene permisos para las secciones disponibles. Pide a un administrador que te asigne
        un rol con los permisos necesarios.
      </p>
    </div>
  `,
})
export class SinAccesoPage {}
