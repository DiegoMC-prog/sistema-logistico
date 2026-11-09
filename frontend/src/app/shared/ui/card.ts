import { Component } from '@angular/core';

@Component({
  selector: 'app-card',
  template: `<ng-content />`,
  host: { class: 'block rounded-xl border border-border bg-surface shadow-card' },
})
export class Card {}
