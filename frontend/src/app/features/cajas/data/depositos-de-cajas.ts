import { Injectable, Signal, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, forkJoin, map, of } from 'rxjs';
import { DepositosService } from '../../depositos/data/depositos.service';
import { CajasService } from './cajas.service';

/** De qué depósito descuenta stock cada caja, para mostrarlo junto a su nombre. */
@Injectable({ providedIn: 'root' })
export class DepositosDeCajas {
  private readonly cajas = inject(CajasService);
  private readonly depositos = inject(DepositosService);

  /** Nombre del depósito por id de caja. Debe llamarse en el contexto de inyección de un componente. */
  porCaja(): Signal<ReadonlyMap<number, string>> {
    const vacio: ReadonlyMap<number, string> = new Map();
    return toSignal(
      forkJoin([this.cajas.list({ size: 100 }), this.depositos.list({ size: 100 })]).pipe(
        map(([cajas, depositos]): ReadonlyMap<number, string> => {
          const nombres = new Map(depositos.content.map((d) => [d.id, d.nombre]));
          return new Map(cajas.content.map((c) => [c.id, nombres.get(c.depositoId) ?? '—']));
        }),
        // Es información de apoyo: si falla, la pantalla sigue funcionando sin ella
        catchError(() => of(vacio)),
      ),
      { initialValue: vacio },
    );
  }
}
