import { Component, computed, inject, input } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

export type IconName =
  | 'activity' | 'alert' | 'barcode' | 'box' | 'camera' | 'check-circle' | 'chevron' | 'clipboard'
  | 'clock' | 'close' | 'download' | 'edit' | 'eye' | 'filter' | 'grid' | 'help' | 'key' | 'location'
  | 'logout' | 'menu' | 'moon' | 'plus' | 'search' | 'settings' | 'shield' | 'sun' | 'tag' | 'trash'
  | 'truck' | 'upload' | 'user' | 'users' | 'cart' | 'wallet' | 'receipt' | 'undo' | 'dollar';

// Trazos tomados del mockup de Figma. Son constantes propias, no contenido externo.
const PATHS: Record<IconName, string> = {
  activity: '<path d="M4 13h3l2-7 4 12 2-5h5" /><path d="M20 5v4h-4" />',
  alert: '<path d="M12 9v4" /><path d="M12 17h.01" /><path d="M10.3 3.6 2.4 17.2A2 2 0 0 0 4.1 20h15.8a2 2 0 0 0 1.7-2.8L13.7 3.6a2 2 0 0 0-3.4 0Z" />',
  barcode: '<path d="M3 5v14M7 5v14M10 5v14M14 5v14M17 5v14M21 5v14" /><path d="M3 9h4m10 6h4" />',
  box: '<path d="m21 8-9-5-9 5 9 5 9-5Z" /><path d="m3 8 9 5v9l9-5V8M12 13v9" />',
  camera: '<path d="M4 7h3l1.5-2h7L17 7h3v12H4Z" /><circle cx="12" cy="13" r="3" />',
  'check-circle': '<circle cx="12" cy="12" r="9" /><path d="m8.5 12.3 2.3 2.3 4.7-4.9" />',
  chevron: '<path d="m9 18 6-6-6-6" />',
  clock: '<circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" />',
  filter: '<path d="M3 5h18l-7 8v6l-4 2v-8L3 5Z" />',
  tag: '<path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8Z" /><circle cx="7.5" cy="7.5" r="1.2" />',
  clipboard: '<rect x="5" y="4" width="14" height="17" rx="2" /><path d="M9 4.5V3h6v1.5M9 10h6m-6 4h6m-6 4h4" />',
  close: '<path d="M6 6l12 12M18 6 6 18" />',
  download: '<path d="M12 3v12m0 0 4-4m-4 4-4-4" /><path d="M5 19h14" />',
  edit: '<path d="m14 4 6 6L9 21H3v-6L14 4Z" /><path d="m12 6 6 6" />',
  eye: '<path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" /><circle cx="12" cy="12" r="2.5" />',
  grid: '<rect x="3" y="3" width="7" height="7" rx="1" /><rect x="14" y="3" width="7" height="7" rx="1" /><rect x="3" y="14" width="7" height="7" rx="1" /><rect x="14" y="14" width="7" height="7" rx="1" />',
  help: '<circle cx="12" cy="12" r="9" /><path d="M9.8 9a2.3 2.3 0 1 1 3.6 1.9c-.8.5-1.4 1-1.4 2.1M12 17h.01" />',
  key: '<circle cx="8" cy="15" r="4" /><path d="m11 12 9-9m-3 3 3 3" />',
  location: '<path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" /><circle cx="12" cy="10" r="2.5" />',
  logout: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" />',
  menu: '<path d="M4 7h16M4 12h16M4 17h16" />',
  moon: '<path d="M20.5 14.1A8 8 0 0 1 9.9 3.5 8.5 8.5 0 1 0 20.5 14Z" />',
  plus: '<path d="M12 5v14M5 12h14" />',
  search: '<circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" />',
  settings: '<circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-2.8 2.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6v.2h-4V21a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1L4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9A1.7 1.7 0 0 0 3 14H3v-4h.1a1.7 1.7 0 0 0 1.5-1 1.7 1.7 0 0 0-.3-1.9L4.2 7 7 4.2l.1.1a1.7 1.7 0 0 0 1.9.3 1.7 1.7 0 0 0 1-1.6v-.2h4V3a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1L19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.2v4H21a1.7 1.7 0 0 0-1.6 1Z" />',
  shield: '<path d="M12 3 4 6v6c0 5 3.5 8 8 9 4.5-1 8-4 8-9V6l-8-3Z" /><path d="m9 12 2 2 4-4" />',
  sun: '<circle cx="12" cy="12" r="4" /><path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />',
  trash: '<path d="M4 7h16M9 7V4h6v3m3 0-1 14H7L6 7" /><path d="M10 11v6m4-6v6" />',
  truck: '<path d="M3 6h11v11H3zM14 10h4l3 3v4h-7z" /><circle cx="7" cy="18" r="2" /><circle cx="17" cy="18" r="2" />',
  cart: '<circle cx="9" cy="20" r="1.5" /><circle cx="18" cy="20" r="1.5" /><path d="M2 3h3l2.7 12.4a1 1 0 0 0 1 .8h8.9a1 1 0 0 0 1-.8L20 7H6" />',
  wallet: '<path d="M3 7a2 2 0 0 1 2-2h13v4" /><path d="M3 7v11a2 2 0 0 0 2 2h15V9H5a2 2 0 0 1-2-2Z" /><circle cx="16.5" cy="14.5" r="1" />',
  receipt: '<path d="M6 3h12v18l-3-2-3 2-3-2-3 2V3Z" /><path d="M9 8h6M9 12h6" />',
  undo: '<path d="M9 14 4 9l5-5" /><path d="M4 9h10a6 6 0 0 1 0 12h-3" />',
  dollar: '<circle cx="12" cy="12" r="9" /><path d="M14.8 9.2c-.4-.9-1.5-1.4-2.8-1.4-1.6 0-2.8.8-2.8 2s1.2 1.7 2.8 2.1 2.8.9 2.8 2.1-1.2 2-2.8 2c-1.3 0-2.4-.6-2.8-1.5M12 6.5v1.3m0 8.4v1.3" />',
  upload: '<path d="M12 16V4m0 0L8 8m4-4 4 4" /><path d="M4 15v5h16v-5" />',
  user: '<circle cx="12" cy="8" r="4" /><path d="M4 21a8 8 0 0 1 16 0" />',
  users: '<circle cx="9" cy="8" r="3.5" /><path d="M2.5 20a6.5 6.5 0 0 1 13 0" /><circle cx="17.5" cy="9" r="2.5" /><path d="M17 14.5a5 5 0 0 1 4.5 5" />',
};

/** <app-icon name="box" class="size-4" /> — sin clase size-* mide 1.25rem. */
@Component({
  selector: 'app-icon',
  template: `<span class="contents" [innerHTML]="svg()"></span>`,
  styles: `
    :host {
      display: inline-flex;
      flex-shrink: 0;
    }
    :host:not([class*='size-']) {
      width: 1.25rem;
      height: 1.25rem;
    }
  `,
  host: { 'aria-hidden': 'true' },
})
export class Icon {
  private readonly sanitizer = inject(DomSanitizer);
  readonly name = input.required<IconName>();

  protected readonly svg = computed(() =>
    this.sanitizer.bypassSecurityTrustHtml(
      `<svg class="size-full" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" viewBox="0 0 24 24">${PATHS[this.name()]}</svg>`,
    ),
  );
}
