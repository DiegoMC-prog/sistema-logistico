import { HttpContext, HttpContextToken } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';

export type ToastTone = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: number;
  tone: ToastTone;
  title: string;
  message?: string;
  duration: number;
}

const TITULOS: Record<ToastTone, string> = {
  success: 'Listo',
  error: 'Error',
  warning: 'Atención',
  info: 'Información',
};

const MAX_VISIBLES = 4;

/**
 * Notificaciones emergentes al costado de la pantalla.
 *
 *   toast.success('Pedido enviado');
 *   toast.error('No se pudo guardar', { title: 'Error al guardar' });
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private seq = 0;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();

  success(message: string, opts?: ToastOptions): void {
    this.show('success', message, opts);
  }

  error(message: string, opts?: ToastOptions): void {
    this.show('error', message, opts);
  }

  warning(message: string, opts?: ToastOptions): void {
    this.show('warning', message, opts);
  }

  info(message: string, opts?: ToastOptions): void {
    this.show('info', message, opts);
  }

  show(tone: ToastTone, message: string, opts: ToastOptions = {}): void {
    // Evita apilar avisos idénticos (por ejemplo varias peticiones que fallan a la vez).
    if (this.toasts().some((t) => t.tone === tone && (t.message ?? t.title) === message)) return;

    const toast: Toast = {
      id: ++this.seq,
      tone,
      title: opts.title ?? TITULOS[tone],
      message,
      duration: opts.duration ?? (tone === 'error' ? 7000 : 4000),
    };
    this.toasts.update((ts) => [...ts, toast].slice(-MAX_VISIBLES));
    this.programar(toast);
  }

  dismiss(id: number): void {
    this.cancelar(id);
    this.toasts.update((ts) => ts.filter((t) => t.id !== id));
  }

  /** Al pasar el cursor por encima, el aviso no se cierra. */
  pausar(id: number): void {
    this.cancelar(id);
  }

  reanudar(id: number): void {
    const t = this.toasts().find((x) => x.id === id);
    if (t) this.programar({ ...t, duration: Math.min(t.duration, 2500) });
  }

  private programar(t: Toast): void {
    this.cancelar(t.id);
    this.timers.set(t.id, setTimeout(() => this.dismiss(t.id), t.duration));
  }

  private cancelar(id: number): void {
    const timer = this.timers.get(id);
    if (timer) clearTimeout(timer);
    this.timers.delete(id);
  }
}

export interface ToastOptions {
  title?: string;
  /** Milisegundos hasta que se cierra solo. */
  duration?: number;
}

/**
 * Mensaje de éxito de una petición. El interceptor muestra uno genérico según el método HTTP;
 * este token lo reemplaza (`withToast('Venta registrada')`) o lo desactiva (`withToast(null)`).
 */
export const TOAST_MESSAGE = new HttpContextToken<string | null | undefined>(() => undefined);

/** Opciones de petición con un mensaje de éxito propio: `http.post(url, body, withToast('Pedido enviado'))`. */
export function withToast(message: string | null): { context: HttpContext } {
  return { context: new HttpContext().set(TOAST_MESSAGE, message) };
}
