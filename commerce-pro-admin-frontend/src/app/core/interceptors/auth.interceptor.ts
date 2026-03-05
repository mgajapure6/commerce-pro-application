import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../services/auth/auth.service';

const isAuthEndpoint = (url: string): boolean =>
  url.includes('/api/v1/auth/login') || url.includes('/api/v1/auth/refresh');

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getAccessToken();
  const userId = authService.getCurrentUserId();

  let authRequest = request;
  if (!isAuthEndpoint(request.url) && token) {
    authRequest = authRequest.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  if (userId && authRequest.url.includes('/api/v1/identity')) {
    authRequest = authRequest.clone({
      setHeaders: {
        'X-User-Id': userId,
        'X-Admin-Id': userId
      }
    });
  }

  return next(authRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || isAuthEndpoint(request.url)) {
        return throwError(() => error);
      }

      return authService.refreshToken().pipe(
        switchMap(success => {
          if (!success) {
            router.navigate(['/auth/login']);
            return throwError(() => error);
          }

          const refreshedToken = authService.getAccessToken();
          const retriedRequest = request.clone({
            setHeaders: refreshedToken ? { Authorization: `Bearer ${refreshedToken}` } : {}
          });

          return next(retriedRequest);
        }),
        catchError(refreshError => {
          router.navigate(['/auth/login']);
          return throwError(() => refreshError);
        })
      );
    })
  );
};
