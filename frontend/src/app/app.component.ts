import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

import { LoadingService } from './core/loading.service';
import { ToastService } from './core/toast.service';

/**
 * Root shell of the app: the top navigation bar, the global loading bar,
 * the router outlet that swaps pages in, and the error toast stack.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  /** Drives the loading bar — counted up/down by the HTTP interceptor. */
  loading = inject(LoadingService);

  /** Error toasts pushed by the HTTP interceptor; template renders + dismisses them. */
  toasts = inject(ToastService);
}
