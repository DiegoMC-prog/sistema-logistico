import { signal } from '@angular/core';
import { Observable } from 'rxjs';
import { errorMessage } from './api-error';
import { Page, PageParams } from './page.model';

/** Estado de una tabla paginada con filtros: carga, error y navegación de páginas. */
export class PagedList<T, F extends PageParams = PageParams> {
  readonly data = signal<Page<T> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly size: number;

  private filters: Partial<F> = {};
  private page = 0;
  private requestId = 0;

  constructor(
    private readonly fetch: (filters: F) => Observable<Page<T>>,
    size = 10,
  ) {
    this.size = size;
    this.load(0);
  }

  load(page: number): void {
    this.page = page;
    const id = ++this.requestId;
    this.loading.set(true);
    this.fetch({ ...this.filters, page, size: this.size } as F).subscribe({
      next: (d) => {
        if (id !== this.requestId) return; // respuesta obsoleta
        // Si se borró el último registro de la página, retrocede.
        if (d.content.length === 0 && page > 0 && d.totalPages > 0) return this.load(d.totalPages - 1);
        this.data.set(d);
        this.error.set(null);
        this.loading.set(false);
      },
      error: (e) => {
        if (id !== this.requestId) return;
        this.error.set(errorMessage(e));
        this.loading.set(false);
      },
    });
  }

  /** Aplica filtros nuevos y vuelve a la primera página. */
  setFilters(patch: Partial<F>): void {
    this.filters = { ...this.filters, ...patch };
    this.load(0);
  }

  reload(): void {
    this.load(this.page);
  }
}
