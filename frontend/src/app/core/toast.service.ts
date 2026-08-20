import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private nextId = 0;

  show(message: string): void {
    const toast: Toast = { id: this.nextId++, message };
    this.toasts.update((all) => [...all, toast]);
    setTimeout(() => this.dismiss(toast.id), 5000);
  }

  dismiss(id: number): void {
    this.toasts.update((all) => all.filter((t) => t.id !== id));
  }
}
