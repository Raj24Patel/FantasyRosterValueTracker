import { Injectable, computed, signal } from '@angular/core';

/**
 * Tracks how many HTTP requests are currently in flight, so the top-level
 * loading bar can show/hide correctly even when multiple requests overlap.
 * Driven entirely by the HTTP interceptor (start/stop per request).
 */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private inFlight = signal(0);

  /** True while at least one request is in flight. */
  readonly loading = computed(() => this.inFlight() > 0);

  /** Call when a request starts. No input/output; increments the in-flight count. */
  start(): void {
    this.inFlight.update((n) => n + 1);
  }

  /** Call when a request finishes (success or error). Never goes below zero. */
  stop(): void {
    this.inFlight.update((n) => Math.max(0, n - 1));
  }
}
