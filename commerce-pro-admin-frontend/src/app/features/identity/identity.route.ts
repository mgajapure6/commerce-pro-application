import { Routes } from '@angular/router';

import { permissionGuard } from '../../core/guards/permission.guard';

export const IDENTITY_ROUTES: Routes = [
  {
    path: 'users',
    canActivate: [permissionGuard],
    data: { authorities: ['identity:user:read'] },
    loadComponent: () => import('./users/users').then(m => m.IdentityUsers)
  },
  {
    path: 'roles',
    canActivate: [permissionGuard],
    data: { authorities: ['identity:role:read', 'identity:role:manage'] },
    loadComponent: () => import('./roles/roles').then(m => m.IdentityRoles)
  },
  {
    path: 'permissions',
    canActivate: [permissionGuard],
    data: { authorities: ['identity:permission:read'] },
    loadComponent: () => import('./permissions/permissions').then(m => m.IdentityPermissions)
  },
  {
    path: 'audit',
    canActivate: [permissionGuard],
    data: { authorities: ['identity:audit:read'] },
    loadComponent: () => import('./audit/audit').then(m => m.IdentityAudit)
  },
  {
    path: 'configuration',
    canActivate: [permissionGuard],
    data: { authorities: ['identity:config:read'] },
    loadComponent: () => import('./configuration/configuration').then(m => m.IdentityConfiguration)
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'users'
  }
];
