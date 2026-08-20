import { Injectable, signal } from '@angular/core';

/** Whether a toast reports a failure or a completed action — drives its accent color. */
export type ToastKind = 'error' | 'success';

/** A single dismissible toast message. */
export interface Toast {
  id: number;
  message: string;
  kind: ToastKind;
}

/**
 * Simple toast queue. Errors (mostly failed API calls, raised by the HTTP
 * interceptor) show in red; confirmations of an action the user took show
 * in green. Toasts self-dismiss after 5 seconds or on click.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  /** Currently visible toasts, oldest first. */
  readonly toasts = signal<Toast[]>([]);
  private nextId = 0;

  /**
   * Queues a red toast for something that went wrong.
   * @param message the text to display
   */
  error(message: string): void {
    this.push(message, 'error');
  }

  /**
   * Queues a green toast confirming an action succeeded.
   * @param message the text to display
   */
  success(message: string): void {
    this.push(message, 'success');
  }

  /** @param id the toast to remove (a no-op if it's already gone) */
  dismiss(id: number): void {
    this.toasts.update((all) => all.filter((t) => t.id !== id));
  }

  /**
   * Adds a toast and schedules its automatic dismissal.
   * @param message the text to display
   * @param kind which accent color to use
   */
  private push(message: string, kind: ToastKind): void {
    const toast: Toast = { id: this.nextId++, message, kind };
    this.toasts.update((all) => [...all, toast]);
    setTimeout(() => this.dismiss(toast.id), 5000);
  }
}
