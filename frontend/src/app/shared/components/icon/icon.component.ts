import { Component, input } from '@angular/core';

export type IconName =
  | 'activity'
  | 'alert'
  | 'barcode'
  | 'box'
  | 'chevron'
  | 'clipboard'
  | 'download'
  | 'edit'
  | 'grid'
  | 'help'
  | 'location'
  | 'menu'
  | 'moon'
  | 'plus'
  | 'search'
  | 'settings'
  | 'sun'
  | 'trash'
  | 'truck'
  | 'user';

@Component({
  selector: 'app-icon',
  standalone: true,
  template: `
    <svg
      aria-hidden="true"
      [class]="svgClass()"
      fill="none"
      stroke="currentColor"
      stroke-linecap="round"
      stroke-linejoin="round"
      stroke-width="1.8"
      viewBox="0 0 24 24"
    >
      @switch (name()) {
        @case ('activity') {
          <path d="M4 13h3l2-7 4 12 2-5h5" />
          <path d="M20 5v4h-4" />
        }
        @case ('alert') {
          <path d="M12 9v4" />
          <path d="M12 17h.01" />
          <path d="M10.3 3.6 2.4 17.2A2 2 0 0 0 4.1 20h15.8a2 2 0 0 0 1.7-2.8L13.7 3.6a2 2 0 0 0-3.4 0Z" />
        }
        @case ('barcode') {
          <path d="M3 5v14M7 5v14M10 5v14M14 5v14M17 5v14M21 5v14" />
          <path d="M3 9h4m10 6h4" />
        }
        @case ('box') {
          <path d="m21 8-9-5-9 5 9 5 9-5Z" />
          <path d="m3 8 9 5v9l9-5V8M12 13v9" />
        }
        @case ('chevron') {
          <path d="m9 18 6-6-6-6" />
        }
        @case ('clipboard') {
          <rect x="5" y="4" width="14" height="17" rx="2" />
          <path d="M9 4.5V3h6v1.5M9 10h6m-6 4h6m-6 4h4" />
        }
        @case ('download') {
          <path d="M12 3v12m0 0 4-4m-4 4-4-4" />
          <path d="M5 19h14" />
        }
        @case ('edit') {
          <path d="m14 4 6 6L9 21H3v-6L14 4Z" />
          <path d="m12 6 6 6" />
        }
        @case ('grid') {
          <rect x="3" y="3" width="7" height="7" rx="1" />
          <rect x="14" y="3" width="7" height="7" rx="1" />
          <rect x="3" y="14" width="7" height="7" rx="1" />
          <rect x="14" y="14" width="7" height="7" rx="1" />
        }
        @case ('help') {
          <circle cx="12" cy="12" r="9" />
          <path d="M9.8 9a2.3 2.3 0 1 1 3.6 1.9c-.8.5-1.4 1-1.4 2.1M12 17h.01" />
        }
        @case ('location') {
          <path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" />
          <circle cx="12" cy="10" r="2.5" />
        }
        @case ('menu') {
          <path d="M4 7h16M4 12h16M4 17h16" />
        }
        @case ('moon') {
          <path d="M20.5 14.1A8 8 0 0 1 9.9 3.5 8.5 8.5 0 1 0 20.5 14Z" />
        }
        @case ('plus') {
          <path d="M12 5v14M5 12h14" />
        }
        @case ('search') {
          <circle cx="11" cy="11" r="7" />
          <path d="m20 20-4-4" />
        }
        @case ('settings') {
          <circle cx="12" cy="12" r="3" />
          <path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-2.8 2.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6v.2h-4V21a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1L4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9A1.7 1.7 0 0 0 3 14H3v-4h.1a1.7 1.7 0 0 0 1.5-1 1.7 1.7 0 0 0-.3-1.9L4.2 7 7 4.2l.1.1a1.7 1.7 0 0 0 1.9.3 1.7 1.7 0 0 0 1-1.6v-.2h4V3a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1L19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.2v4H21a1.7 1.7 0 0 0-1.6 1Z" />
        }
        @case ('sun') {
          <circle cx="12" cy="12" r="4" />
          <path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
        }
        @case ('trash') {
          <path d="M4 7h16M9 7V4h6v3m3 0-1 14H7L6 7" />
          <path d="M10 11v6m4-6v6" />
        }
        @case ('truck') {
          <path d="M3 6h11v11H3zM14 10h4l3 3v4h-7z" />
          <circle cx="7" cy="18" r="2" />
          <circle cx="17" cy="18" r="2" />
        }
        @case ('user') {
          <circle cx="12" cy="8" r="4" />
          <path d="M4 21a8 8 0 0 1 16 0" />
        }
      }
    </svg>
  `,
})
export class IconComponent {
  name = input.required<IconName>();
  svgClass = input<string>('size-5');
}
