import { DOCUMENT } from '@angular/common';
import { Injectable, effect, inject, signal } from '@angular/core';

const KEY = 'theme.dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly doc = inject(DOCUMENT);
  readonly dark = signal(this.initial());

  constructor() {
    effect(() => {
      const dark = this.dark();
      this.doc.documentElement.classList.toggle('dark', dark);
      try {
        localStorage.setItem(KEY, String(dark));
      } catch {
        /* sin almacenamiento: no se recuerda la preferencia */
      }
    });
  }

  toggle(): void {
    this.dark.update((v) => !v);
  }

  private initial(): boolean {
    try {
      const saved = localStorage.getItem(KEY);
      if (saved !== null) return saved === 'true';
    } catch {
      /* ignorar */
    }
    return this.doc.defaultView?.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
  }
}
