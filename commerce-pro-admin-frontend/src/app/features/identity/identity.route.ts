import { Routes } from '@angular/router';

export const IDENTITY_ROUTES: Routes = [
  {
    path: 'users',
    loadComponent: () => import('./users/users').then(m => m.IdentityUsers)
  },
  {
    path: 'roles',
    loadComponent: () => import('./roles/roles').then(m => m.IdentityRoles)
  },
  {
    path: 'permissions',
    loadComponent: () => import('./permissions/permissions').then(m => m.IdentityPermissions)
  },
  {
    path: 'audit',
    loadComponent: () => import('./audit/audit').then(m => m.IdentityAudit)
  },
  {
    path: 'configuration',
    loadComponent: () => import('./configuration/configuration').then(m => m.IdentityConfiguration)
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'users'
  }
];
