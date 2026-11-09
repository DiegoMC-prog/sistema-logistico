import { Directive, TemplateRef, ViewContainerRef, effect, inject, input } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';

/** <button *appHasPermission="'NOMBRE_PERMISO'">Nuevo</button> */
@Directive({ selector: '[appHasPermission]' })
export class HasPermissionDirective {
  private readonly auth = inject(AuthService);
  private readonly tpl = inject(TemplateRef);
  private readonly vcr = inject(ViewContainerRef);
  readonly appHasPermission = input.required<string>();

  constructor() {
    effect(() => {
      this.vcr.clear();
      this.auth.perfil(); // dependencia reactiva: se reevalúa al cargar el perfil
      if (this.auth.hasPermission(this.appHasPermission())) this.vcr.createEmbeddedView(this.tpl);
    });
  }
}
