import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth/auth.service';

export const permissionGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const requiredAuthorities = (route.data?.['authorities'] as string[] | undefined) ?? [];
  if (authService.hasAnyAuthority(requiredAuthorities)) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
