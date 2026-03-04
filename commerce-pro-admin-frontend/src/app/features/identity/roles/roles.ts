import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CreateIdentityRoleRequest, IdentityRole } from '../../../core/models/identity';
import { IdentityService } from '../../../core/services/identity/identity.service';

@Component({
  selector: 'app-identity-roles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './roles.html',
  styleUrl: './roles.scss'
})
export class IdentityRoles implements OnInit {
  private readonly identityService = inject(IdentityService);

  readonly roles = signal<IdentityRole[]>([]);
  readonly selectedRoleId = signal('');
  readonly permissionCodesInput = signal('');

  readonly createRoleForm = signal<CreateIdentityRoleRequest>({
    code: '',
    name: '',
    description: '',
    permissionCodes: []
  });

  readonly totalRoles = computed(() => this.roles().length);

  ngOnInit(): void {
    this.identityService.getRoles(0, 30).subscribe(page => this.roles.set(page.content));
  }

  updateCreateRoleField(field: keyof CreateIdentityRoleRequest, value: string): void {
    this.createRoleForm.update(current => ({ ...current, [field]: value }));
  }

  createRole(): void {
    this.identityService.createRole(this.createRoleForm()).subscribe(role => {
      if (!role) return;
      this.roles.update(list => [role, ...list]);
      this.createRoleForm.set({ code: '', name: '', description: '', permissionCodes: [] });
    });
  }

  mapPermissionsToRole(): void {
    if (!this.selectedRoleId()) return;

    const permissionCodes = this.permissionCodesInput().split(',').map(value => value.trim()).filter(Boolean);
    if (!permissionCodes.length) return;

    this.identityService.grantPermissions(this.selectedRoleId(), permissionCodes).subscribe(success => {
      if (success) this.permissionCodesInput.set('');
    });
  }
}
