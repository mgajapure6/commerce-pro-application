import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CreateIdentityPermissionRequest, IdentityPermission } from '../../../core/models/identity';
import { IdentityService } from '../../../core/services/identity/identity.service';

@Component({
  selector: 'app-identity-permissions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './permissions.html',
  styleUrl: './permissions.scss'
})
export class IdentityPermissions implements OnInit {
  private readonly identityService = inject(IdentityService);

  readonly permissions = signal<IdentityPermission[]>([]);

  readonly createPermissionForm = signal<CreateIdentityPermissionRequest>({
    code: '',
    name: '',
    description: '',
    category: 'SYSTEM',
    riskLevel: 1,
    requiresApproval: false,
    applicableScopes: []
  });

  readonly totalPermissions = computed(() => this.permissions().length);

  ngOnInit(): void {
    this.identityService.getPermissions(0, 50).subscribe(page => this.permissions.set(page.content));
  }

  updateCreatePermissionField(field: keyof CreateIdentityPermissionRequest, value: string | number | boolean): void {
    this.createPermissionForm.update(current => ({ ...current, [field]: value }));
  }

  createPermission(): void {
    this.identityService.createPermission(this.createPermissionForm()).subscribe(permission => {
      if (!permission) return;
      this.permissions.update(list => [permission, ...list]);
      this.createPermissionForm.set({
        code: '',
        name: '',
        description: '',
        category: 'SYSTEM',
        riskLevel: 1,
        requiresApproval: false,
        applicableScopes: []
      });
    });
  }
}
