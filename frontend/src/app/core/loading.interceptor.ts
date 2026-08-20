import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, finalize, throwError } from 'rxjs';

import { LoadingService } from './loading.service';
import { ToastService } from './toast.service';

/** Global loading bar + error toast for every API call. */
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loading = inject(LoadingService);
  const toast = inject(ToastService);

  loading.start();
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      // the backend returns RFC 7807 problem details with a human-readable message
      const detail = err.error?.detail ?? `Request failed (${err.status || 'network error'})`;
      toast.show(detail);
      return throwError(() => err);
    }),
    finalize(() => loading.stop())
  );
};
