import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoriaService } from '../../categoria.service';
import { Categoria, CategoriaStats } from '../../categoria.model';
import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { ErrorHandlerService } from '../../../../core/services/error-handler.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-categoria-list',
  standalone: true,
  imports: [CommonModule, FormsModule, IconComponent],
  templateUrl: './categoria-list.html',
})
export class CategoriaList implements OnInit {
  private readonly categoriaService = inject(CategoriaService);
  private readonly errorHandler = inject(ErrorHandlerService);
  private readonly toastService = inject(ToastService);

  categorias = signal<Categoria[]>([]);
  stats = signal<CategoriaStats>({ totalCategorias: 0, categoriasEnUso: 0 });
  loading = signal<boolean>(false);
  guardando = signal<boolean>(false);
  paginaActual = signal<number>(0);
  totalPaginas = signal<number>(0);
  totalElementos = signal<number>(0);

  filtroNombre = '';
  nombreInput = '';
  errorInput = signal<string>('');
  categoriaEditando = signal<Categoria | null>(null);
  mostrarModal = signal<boolean>(false);

  ngOnInit(): void {
    this.cargarDatos();
    this.cargarEstadisticas();
  }

  cargarDatos(pagina: number = 0): void {
    this.loading.set(true);
    this.categoriaService.listar(pagina, 10, this.filtroNombre).subscribe({
      next: (res) => {
        this.categorias.set(res.content);
        this.paginaActual.set(res.page ?? res.number);
        this.totalPaginas.set(res.totalPages);
        this.totalElementos.set(res.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorHandler.notificarError(err, 'Error al cargar categorías');
      },
    });
  }

  cargarEstadisticas(): void {
    this.categoriaService.obtenerEstadisticas().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error('Error al cargar estadísticas de categorías', err),
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
    this.categoriaEditando.set(null);
    this.nombreInput = '';
    this.errorInput.set('');
    this.mostrarModal.set(true);
  }

  abrirEditar(cat: Categoria): void {
    this.categoriaEditando.set(cat);
    this.nombreInput = cat.nombre;
    this.errorInput.set('');
    this.mostrarModal.set(true);
  }

  cerrarModal(): void {
    this.mostrarModal.set(false);
    this.errorInput.set('');
  }

  guardar(): void {
    const nombre = this.nombreInput.trim();
    if (!nombre) {
      this.errorInput.set('El nombre de la categoría es obligatorio.');
      return;
    }

    this.guardando.set(true);
    const editando = this.categoriaEditando();

    if (editando) {
      this.categoriaService.actualizar(editando.id, nombre).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.cargarDatos(this.paginaActual());
          this.cargarEstadisticas();
          this.toastService.success('Categoría actualizada', 'Los cambios se guardaron con éxito.');
        },
        error: (err) => {
          this.guardando.set(false);
          const errores = this.errorHandler.extraerErroresCampos(err);
          if (errores['nombre']) {
            this.errorInput.set(errores['nombre']);
          } else {
            const mensaje = this.errorHandler.extraerMensaje(err, 'Error al actualizar categoría');
            this.errorInput.set(mensaje);
            this.toastService.error('Error al actualizar', mensaje);
          }
        },
      });
    } else {
      this.categoriaService.crear(nombre).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cerrarModal();
          this.cargarDatos(0);
          this.cargarEstadisticas();
          this.toastService.success('Categoría creada', 'La categoría se registró correctamente.');
        },
        error: (err) => {
          this.guardando.set(false);
          const errores = this.errorHandler.extraerErroresCampos(err);
          if (errores['nombre']) {
            this.errorInput.set(errores['nombre']);
          } else {
            const mensaje = this.errorHandler.extraerMensaje(err, 'Error al crear categoría');
            this.errorInput.set(mensaje);
            this.toastService.error('Error al crear', mensaje);
          }
        },
      });
    }
  }

  eliminar(cat: Categoria): void {
    if (confirm(`¿Eliminar la categoría "${cat.nombre}"?`)) {
      this.categoriaService.eliminar(cat.id).subscribe({
        next: () => {
          this.cargarDatos(this.paginaActual());
          this.cargarEstadisticas();
          this.toastService.success('Categoría eliminada', `"${cat.nombre}" ha sido eliminada.`);
        },
        error: (err) => {
          this.errorHandler.notificarError(err, 'No se pudo eliminar la categoría');
        },
      });
    }
  }


  readonly pageSize = 10;

  formatearCorrelativo(index: number): string {
    const numero = this.paginaActual() * this.pageSize + index + 1;
    return `#${String(numero).padStart(3, '0')}`;
  }
}

