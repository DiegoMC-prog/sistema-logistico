import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DepositoService } from '../../deposito.service';
import { Deposito, DepositoStats, DepositoRequest } from '../../deposito.model';
import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { ErrorHandlerService } from '../../../../core/services/error-handler.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-deposito-list',
  standalone: true,
  imports: [CommonModule, FormsModule, IconComponent],
  templateUrl: './deposito-list.html',
})
export class DepositoList implements OnInit {
  private readonly depositoService = inject(DepositoService);
  private readonly errorHandler = inject(ErrorHandlerService);
  private readonly toastService = inject(ToastService);

  depositos = signal<Deposito[]>([]);
  stats = signal<DepositoStats>({ totalDepositos: 0, activos: 0, inactivos: 0 });
  loading = signal<boolean>(false);
  guardando = signal<boolean>(false);
  cambiandoEstadoId = signal<number | null>(null);

  paginaActual = signal<number>(0);
  totalPaginas = signal<number>(0);
  totalElementos = signal<number>(0);

  filtroNombre = '';
  nombreInput = '';
  descripcionInput = '';
  errorNombre = signal<string>('');
  depositoEditando = signal<Deposito | null>(null);
  mostrarModal = signal<boolean>(false);

  readonly pageSize = 10;

  ngOnInit(): void {
    this.cargarDatos();
    this.cargarEstadisticas();
  }

  cargarDatos(pagina: number = 0): void {
    this.loading.set(true);
    this.depositoService.listar(pagina, this.pageSize, this.filtroNombre).subscribe({
      next: (res) => {
        this.depositos.set(res.content);
        this.paginaActual.set(res.page ?? res.number);
        this.totalPaginas.set(res.totalPages);
        this.totalElementos.set(res.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorHandler.notificarError(err, 'Error al cargar depósitos');
      },
    });
  }

  cargarEstadisticas(): void {
    this.depositoService.obtenerEstadisticas().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error('Error al cargar estadísticas de depósitos', err),
    });
  }

  onBuscar(): void {
    this.cargarDatos(0);
  }

  limpiarBusqueda(): void {
    this.filtroNombre = '';
    this.cargarDatos(0);
  }

  abrirCrear(): void {
    this.depositoEditando.set(null);
    this.nombreInput = '';
    this.descripcionInput = '';
    this.errorNombre.set('');
    this.mostrarModal.set(true);
  }

  abrirEditar(dep: Deposito): void {
    this.depositoEditando.set(dep);
    this.nombreInput = dep.nombre;
    this.descripcionInput = dep.descripcion || '';
    this.errorNombre.set('');
    this.mostrarModal.set(true);
  }

  cerrarModal(): void {
    this.mostrarModal.set(false);
    this.errorNombre.set('');
  }

  guardar(): void {
    const nombre = this.nombreInput.trim();
    if (!nombre) {
      this.errorNombre.set('El nombre del depósito es obligatorio.');
      return;
    }

    const payload: DepositoRequest = {
      nombre,
      descripcion: this.descripcionInput.trim() || undefined,
    };

    this.guardando.set(true);
    const editando = this.depositoEditando();

    if (editando) {
      this.depositoService.actualizar(editando.id, payload).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.cargarDatos(this.paginaActual());
          this.cargarEstadisticas();
          this.toastService.success('Depósito actualizado', 'Los cambios se guardaron con éxito.');
        },
        error: (err) => {
          this.guardando.set(false);
          const errores = this.errorHandler.extraerErroresCampos(err);
          if (errores['nombre']) {
            this.errorNombre.set(errores['nombre']);
          } else {
            const mensaje = this.errorHandler.extraerMensaje(err, 'Error al actualizar depósito');
            this.errorNombre.set(mensaje);
            this.toastService.error('Error al actualizar', mensaje);
          }
        },
      });
    } else {
      this.depositoService.crear(payload).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.cargarDatos(0);
          this.cargarEstadisticas();
          this.toastService.success('Depósito creado', 'El depósito se registró correctamente.');
        },
        error: (err) => {
          this.guardando.set(false);
          const errores = this.errorHandler.extraerErroresCampos(err);
          if (errores['nombre']) {
            this.errorNombre.set(errores['nombre']);
          } else {
            const mensaje = this.errorHandler.extraerMensaje(err, 'Error al crear depósito');
            this.errorNombre.set(mensaje);
            this.toastService.error('Error al crear', mensaje);
          }
        },
      });
    }
  }

  toggleEstado(dep: Deposito): void {
    const nuevoEstado = !dep.estado;
    this.cambiandoEstadoId.set(dep.id);

    this.depositoService.cambiarEstado(dep.id, nuevoEstado).subscribe({
      next: (actualizado) => {
        this.cambiandoEstadoId.set(null);
        // Actualizar el depósito localmente
        this.depositos.update((items) =>
          items.map((item) => (item.id === dep.id ? { ...item, estado: actualizado.estado } : item))
        );
        this.cargarEstadisticas();
        this.toastService.info(
          'Estado actualizado',
          `"${dep.nombre}" ahora está ${actualizado.estado ? 'Activo' : 'Inactivo'}.`
        );
      },
      error: (err) => {
        this.cambiandoEstadoId.set(null);
        this.errorHandler.notificarError(err, 'No se pudo cambiar el estado');
      },
    });
  }

  eliminar(dep: Deposito): void {
    if (confirm(`¿Eliminar el depósito "${dep.nombre}"?`)) {
      this.depositoService.eliminar(dep.id).subscribe({
        next: () => {
          this.cargarDatos(this.paginaActual());
          this.cargarEstadisticas();
          this.toastService.success('Depósito eliminado', `"${dep.nombre}" ha sido eliminado.`);
        },
        error: (err) => {
          this.errorHandler.notificarError(err, 'No se pudo eliminar el depósito');
        },
      });
    }
  }

  formatearCorrelativo(index: number): string {
    const numero = this.paginaActual() * this.pageSize + index + 1;
    return `#${String(numero).padStart(3, '0')}`;
  }
}
