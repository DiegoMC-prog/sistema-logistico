import { Injectable, signal } from '@angular/core';

export type ToastTone = 'success' | 'error' | 'warning' | 'info';

export interface ToastItem {
  id: number;
  tone: ToastTone;
  title: string;
  message?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private counter = 0;
  readonly toasts = signal<ToastItem[]>([]);

  show(tone: ToastTone, title: string, message?: string, durationMs = 4000): void {
    const id = ++this.counter;
    const item: ToastItem = { id, tone, title, message };

    this.toasts.update((current) => [...current, item]);

    if (durationMs > 0) {
      setTimeout(() => {
        this.dismiss(id);
      }, durationMs);
    }
  }

  success(title: string, message?: string): void {
    this.show('success', title, message);
  }

  error(title: string, message?: string): void {
    this.show('error', title, message, 5000);
  }

  warning(title: string, message?: string): void {
    this.show('warning', title, message);
  }

  info(title: string, message?: string): void {
    this.show('info', title, message);
  }

  dismiss(id: number): void {
    this.toasts.update((current) => current.filter((t) => t.id !== id));
  }
}
