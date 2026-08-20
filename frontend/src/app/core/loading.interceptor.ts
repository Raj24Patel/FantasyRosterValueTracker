import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, finalize, throwError } from 'rxjs';

import { LoadingService } from './loading.service';
import { ToastService } from './toast.service';

/**
 * Wraps every outgoing HTTP request: toggles the global loading bar for its
 * duration, and turns any error into a toast (using the backend's RFC 7807
 * `detail` field when present) before re-throwing so callers can still react.
 * Input/output: standard HttpInterceptorFn signature — passes the request
 * through unchanged, just observes it.
 */
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loading = inject(LoadingService);
  const toast = inject(ToastService);

  loading.start();
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      // the backend returns RFC 7807 problem details with a human-readable message
      const detail = err.error?.detail ?? `Request failed (${err.status || 'network error'})`;
      toast.error(detail);
      return throwError(() => err);
    }),
    finalize(() => loading.stop())
  );
};
