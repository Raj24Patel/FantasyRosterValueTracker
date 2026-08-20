import { Injectable, signal } from '@angular/core';

/** A single dismissible toast message. */
export interface Toast {
  id: number;
  message: string;
}

/**
 * Simple toast queue for surfacing error messages (mainly failed API calls,
 * via the HTTP interceptor). Toasts self-dismiss after 5 seconds or on click.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  /** Currently visible toasts, oldest first. */
  readonly toasts = signal<Toast[]>([]);
  private nextId = 0;

  /**
   * Queues a new toast and auto-dismisses it after 5 seconds.
   * @param message the text to display
   */
  show(message: string): void {
    const toast: Toast = { id: this.nextId++, message };
    this.toasts.update((all) => [...all, toast]);
    setTimeout(() => this.dismiss(toast.id), 5000);
  }

  /** @param id the toast to remove (a no-op if it's already gone) */
  dismiss(id: number): void {
    this.toasts.update((all) => all.filter((t) => t.id !== id));
  }
}
