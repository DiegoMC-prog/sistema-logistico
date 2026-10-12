import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../producto.service';
import { ProductoResponse, ProductoRequest, ProductoStats } from '../../producto.model';
import { CategoriaService } from '../../../categoria/categoria.service';
import { Categoria } from '../../../categoria/categoria.model';
import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { ErrorHandlerService } from '../../../../core/services/error-handler.service';
import { ToastService } from '../../../../core/services/toast.service';
import { environment } from '../../../../../environments/environment.development';

type ModalMode = 'create' | 'edit' | 'view' | 'delete' | null;

@Component({
  selector: 'app-producto-list',
  standalone: true,
  imports: [CommonModule, FormsModule, IconComponent],
  templateUrl: './producto-list.html',
})
export class ProductoList implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly errorHandler = inject(ErrorHandlerService);
  private readonly toastService = inject(ToastService);

  productos = signal<ProductoResponse[]>([]);
  categorias = signal<Categoria[]>([]);
  stats = signal<ProductoStats>({ totalProductos: 0, activos: 0, inactivos: 0 });

  loading = signal<boolean>(false);
  guardando = signal<boolean>(false);
  eliminando = signal<boolean>(false);

  paginaActual = signal<number>(0);
  totalPaginas = signal<number>(0);
  totalElementos = signal<number>(0);

  // Filtros
  filtroNombre = '';
  filtroCategoriaId: number | null = null;
  filtroEstado: boolean | null = null;

  // Modales
  modalMode = signal<ModalMode>(null);
  productoSeleccionado = signal<ProductoResponse | null>(null);

  // Formulario
  formCodigoBarras = '';
  formNombre = '';
  formDescripcion = '';
  formCategoriaId: number | null = null;
  formEstado = true;
  formImagen = signal<string>('');
  formImagenFile: File | null = null;

  // Errores de validación por campo
  erroresCampos = signal<Record<string, string>>({});

  readonly pageSize = 10;

  ngOnInit(): void {
    this.cargarDatos();
    this.cargarEstadisticas();
    this.cargarCategorias();
  }

  cargarDatos(pagina: number = 0): void {
    this.loading.set(true);
    this.productoService
      .listar(pagina, this.pageSize, this.filtroNombre, this.filtroCategoriaId, this.filtroEstado)
      .subscribe({
        next: (res) => {
          this.productos.set(res.content);
          this.paginaActual.set(res.page ?? res.number);
          this.totalPaginas.set(res.totalPages);
          this.totalElementos.set(res.totalElements);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.errorHandler.notificarError(err, 'Error al cargar productos');
        },
      });
  }

  cargarEstadisticas(): void {
    this.productoService.obtenerEstadisticas().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error('Error al cargar estadísticas de productos', err),
    });
  }

  cargarCategorias(): void {
    this.categoriaService.listar(0, 100).subscribe({
      next: (res) => this.categorias.set(res.content),
      error: (err) => console.error('Error al cargar lista de categorías', err),
    });
  }

  onBuscar(): void {
    this.cargarDatos(0);
  }

  limpiarFiltros(): void {
    this.filtroNombre = '';
    this.filtroCategoriaId = null;
    this.filtroEstado = null;
    this.cargarDatos(0);
  }

  abrirCrear(): void {
    this.productoSeleccionado.set(null);
    this.formCodigoBarras = '';
    this.formNombre = '';
    this.formDescripcion = '';
    this.formCategoriaId = this.categorias().length > 0 ? this.categorias()[0].id : null;
    this.formEstado = true;
    this.formImagen.set('');
    this.formImagenFile = null;
    this.erroresCampos.set({});
    this.modalMode.set('create');
  }

  abrirEditar(p: ProductoResponse): void {
    this.productoSeleccionado.set(p);
    this.formCodigoBarras = p.codigoBarras;
    this.formNombre = p.nombre;
    this.formDescripcion = p.descripcion || '';
    this.formCategoriaId = p.categoriaId;
    this.formEstado = p.estado;
    this.formImagen.set(p.imagen || '');
    this.formImagenFile = null;
    this.erroresCampos.set({});
    this.modalMode.set('edit');
  }

  abrirVer(p: ProductoResponse): void {
    this.productoSeleccionado.set(p);
    this.modalMode.set('view');
  }

  abrirEliminar(p: ProductoResponse): void {
    this.productoSeleccionado.set(p);
    this.modalMode.set('delete');
  }

  cerrarModal(): void {
    this.modalMode.set(null);
    this.erroresCampos.set({});
  }

  onImagenSeleccionada(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.procesarArchivoImagen(input.files[0]);
      input.value = '';
    }
  }

  onFileDropped(event: DragEvent): void {
    event.preventDefault();
    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.procesarArchivoImagen(event.dataTransfer.files[0]);
    }
  }

  private procesarArchivoImagen(file: File): void {
    this.formImagenFile = file;
    this.formImagen.set(URL.createObjectURL(file));
  }

  removerImagen(): void {
    this.formImagen.set('');
    this.formImagenFile = null;
  }

  resolverImagen(ruta?: string | null): string {
    if (!ruta) return '';
    const r = ruta.trim();
    if (!r) return '';
    if (
      r.startsWith('http://') ||
      r.startsWith('https://') ||
      r.startsWith('data:') ||
      r.startsWith('blob:')
    ) {
      return r;
    }
    if (r.startsWith('/uploads/') || r.startsWith('uploads/')) {
      const baseUrl = environment.apiUrl.replace(/\/api\/v1\/?$/, '');
      return `${baseUrl}${r.startsWith('/') ? '' : '/'}${r}`;
    }
    return '';
  }

  cambiandoEstadoId = signal<number | null>(null);

  toggleEstado(prod: ProductoResponse): void {
    const nuevoEstado = !prod.estado;
    this.cambiandoEstadoId.set(prod.id);

    this.productoService.cambiarEstado(prod.id, nuevoEstado).subscribe({
      next: (actualizado) => {
        this.cambiandoEstadoId.set(null);
        this.productos.update((items) =>
          items.map((item) => (item.id === prod.id ? { ...item, estado: actualizado.estado } : item))
        );
        this.cargarEstadisticas();
        this.toastService.info(
          'Estado actualizado',
          `"${prod.nombre}" ahora está ${actualizado.estado ? 'Activo' : 'Inactivo'}.`
        );
      },
      error: (err) => {
        this.cambiandoEstadoId.set(null);
        this.errorHandler.notificarError(err, 'No se pudo cambiar el estado del producto');
      },
    });
  }

  private finalizarGuardado(titulo: string, mensaje: string, irAPrimeraPagina = false): void {
    this.guardando.set(false);
    this.cerrarModal();
    this.cargarDatos(irAPrimeraPagina ? 0 : this.paginaActual());
    this.cargarEstadisticas();
    this.toastService.success(titulo, mensaje);
  }

  guardar(): void {
    this.erroresCampos.set({});

    if (!this.formCodigoBarras.trim()) {
      this.erroresCampos.update((errs) => ({ ...errs, codigoBarras: 'El código de barras es obligatorio.' }));
    }
    if (!this.formNombre.trim()) {
      this.erroresCampos.update((errs) => ({ ...errs, nombre: 'El nombre del producto es obligatorio.' }));
    }
    if (!this.formCategoriaId) {
      this.erroresCampos.update((errs) => ({ ...errs, categoriaId: 'Debes seleccionar una categoría.' }));
    }

    if (Object.keys(this.erroresCampos()).length > 0) {
      return;
    }

    const imagenDto = this.formImagenFile
      ? undefined
      : (this.formImagen() ? this.formImagen() : '');

    const payload: ProductoRequest = {
      codigoBarras: this.formCodigoBarras.trim(),
      nombre: this.formNombre.trim(),
      descripcion: this.formDescripcion.trim() || undefined,
      imagen: imagenDto,
      categoriaId: Number(this.formCategoriaId),
    };

    this.guardando.set(true);
    const seleccionado = this.productoSeleccionado();

    if (this.modalMode() === 'edit' && seleccionado) {
      this.productoService.actualizar(seleccionado.id, payload, this.formImagenFile).subscribe({
        next: (actualizado) => {
          if (seleccionado.estado !== this.formEstado) {
            this.productoService.cambiarEstado(actualizado.id, this.formEstado).subscribe({
              next: () => {
                this.finalizarGuardado('Producto actualizado', 'Los cambios se guardaron con éxito.');
              },
              error: (err) => {
                this.errorHandler.notificarError(err, 'Se actualizó la información pero falló el cambio de estado');
                this.finalizarGuardado('Producto actualizado', 'Los cambios se guardaron.');
              },
            });
          } else {
            this.finalizarGuardado('Producto actualizado', 'Los cambios se guardaron con éxito.');
          }
        },
        error: (err) => {
          this.guardando.set(false);
          const errores = this.errorHandler.extraerErroresCampos(err);
          if (Object.keys(errores).length > 0) {
            this.erroresCampos.set(errores);
          } else {
            this.errorHandler.notificarError(err, 'Error al actualizar producto');
          }
        },
      });
    } else {
      this.productoService.crear(payload, this.formImagenFile).subscribe({
        next: (creado) => {
          if (!this.formEstado) {
            this.productoService.cambiarEstado(creado.id, false).subscribe({
              next: () => {
                this.finalizarGuardado('Producto creado', 'El producto se registró en el catálogo.', true);
              },
              error: () => {
                this.finalizarGuardado('Producto creado', 'El producto se registró en el catálogo.', true);
              },
            });
          } else {
            this.finalizarGuardado('Producto creado', 'El producto se registró en el catálogo.', true);
          }
        },
        error: (err) => {
          this.guardando.set(false);
          const errores = this.errorHandler.extraerErroresCampos(err);
          if (Object.keys(errores).length > 0) {
            this.erroresCampos.set(errores);
          } else {
            this.errorHandler.notificarError(err, 'Error al crear producto');
          }
        },
      });
    }
  }


  confirmarEliminar(): void {
    const seleccionado = this.productoSeleccionado();
    if (!seleccionado) return;

    this.eliminando.set(true);
    this.productoService.eliminar(seleccionado.id).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.cerrarModal();
        this.cargarDatos(this.paginaActual());
        this.cargarEstadisticas();
        this.toastService.success('Producto eliminado', `"${seleccionado.nombre}" fue eliminado del catálogo.`);
      },
      error: (err) => {
        this.eliminando.set(false);
        this.errorHandler.notificarError(err, 'No se pudo eliminar el producto');
      },
    });
  }

  formatearCorrelativo(index: number): string {
    const numero = this.paginaActual() * this.pageSize + index + 1;
    return `#${String(numero).padStart(3, '0')}`;
  }
}
