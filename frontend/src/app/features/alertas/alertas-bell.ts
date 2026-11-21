import { DestroyRef, Component, computed, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, EMPTY, switchMap, timer } from 'rxjs';
import { Icon } from '../../shared/ui/icon';
import { AlertasService } from './data/alertas.service';

/** Cada cuánto se consulta el contador (ms). La generación es diaria; esto solo refleja lecturas y cambios. */
const INTERVALO_MS = 60_000;

/** Campana del encabezado con el número de alertas sin leer. Solo se monta si el usuario tiene ALERTA_VER. */
@Component({
  selector: 'app-alertas-bell',
  imports: [RouterLink, Icon],
  template: `
    <a
      routerLink="/alertas"
      class="relative grid size-9 place-items-center rounded-lg text-muted transition duration-150 hover:scale-110 hover:bg-subtle hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent"
      [attr.aria-label]="etiqueta()"
      [attr.title]="etiqueta()"
    >
      <app-icon name="bell" class="size-4" />
      @if (total() > 0) {
        <span
          class="absolute -right-0.5 -top-0.5 grid h-4 min-w-4 place-items-center rounded-full bg-danger px-1 text-[10px] font-bold leading-none text-white"
        >
          {{ total() > 99 ? '99+' : total() }}
        </span>
      }
    </a>
  `,
})
export class AlertasBell {
  private readonly service = inject(AlertasService);

  protected readonly total = computed(() => this.service.conteo()?.total ?? 0);
  protected readonly etiqueta = computed(() =>
    this.total() === 0 ? 'Alertas: ninguna sin leer' : `Alertas: ${this.total()} sin leer`,
  );

  constructor() {
    timer(0, INTERVALO_MS)
      .pipe(
        // Un error (p. ej. sin conexión) no corta el sondeo: se reintenta en el próximo ciclo
        switchMap(() => this.service.refrescarConteo().pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe();
  }
}
