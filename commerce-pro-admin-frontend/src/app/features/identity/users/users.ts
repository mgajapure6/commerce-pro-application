import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CreateIdentityUserRequest, IdentityUser } from '../../../core/models/identity';
import { IdentityService } from '../../../core/services/identity/identity.service';

@Component({
  selector: 'app-identity-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.html',
  styleUrl: './users.scss'
})
export class IdentityUsers implements OnInit {
  private readonly identityService = inject(IdentityService);

  readonly users = signal<IdentityUser[]>([]);
  readonly isLoading = signal(false);
  readonly searchQuery = signal('');
  readonly selectedUserId = signal('');
  readonly roleCodesInput = signal('');

  readonly createUserForm = signal<CreateIdentityUserRequest>({
    username: '',
    email: '',
    password: '',
    sendWelcomeEmail: true,
    initialRoleCodes: []
  });

  readonly filteredUsers = computed(() => {
    const query = this.searchQuery().trim().toLowerCase();
    if (!query) return this.users();
    return this.users().filter(user => [user.username, user.email].some(value => value.toLowerCase().includes(query)));
  });

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading.set(true);
    this.identityService.getUsers(0, 30).subscribe(page => {
      this.users.set(page.content);
      this.isLoading.set(false);
    });
  }

  updateCreateUserField(field: keyof CreateIdentityUserRequest, value: string | boolean): void {
    this.createUserForm.update(current => ({ ...current, [field]: value }));
  }

  createUser(): void {
    this.identityService.createUser(this.createUserForm()).subscribe(user => {
      if (!user) return;
      this.users.update(list => [user, ...list]);
      this.createUserForm.set({ username: '', email: '', password: '', sendWelcomeEmail: true, initialRoleCodes: [] });
    });
  }

  toggleUserStatus(user: IdentityUser): void {
    this.identityService.toggleUserActivation(user.id, !user.active).subscribe(success => {
      if (!success) return;
      this.users.update(list => list.map(value => value.id === user.id ? { ...value, active: !value.active } : value));
    });
  }

  mapRolesToUser(): void {
    if (!this.selectedUserId()) return;

    const roleCodes = this.roleCodesInput().split(',').map(value => value.trim()).filter(Boolean);
    if (!roleCodes.length) return;

    this.identityService.assignRoles(this.selectedUserId(), roleCodes).subscribe(success => {
      if (success) this.roleCodesInput.set('');
    });
  }
}
