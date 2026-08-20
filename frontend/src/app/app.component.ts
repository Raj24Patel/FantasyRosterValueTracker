import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

import { LoadingService } from './core/loading.service';
import { ToastService } from './core/toast.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <div class="loading-bar" [class.active]="loading.loading()"></div>
    <header class="topbar">
      <a routerLink="/leagues" class="brand">🏈 Dynasty Roster Value Tracker</a>
    </header>
    <main class="container">
      <router-outlet />
    </main>
    <div class="toasts">
      @for (toast of toasts.toasts(); track toast.id) {
        <div class="toast" (click)="toasts.dismiss(toast.id)">{{ toast.message }}</div>
      }
    </div>
  `
})
export class AppComponent {
  loading = inject(LoadingService);
  toasts = inject(ToastService);
}
