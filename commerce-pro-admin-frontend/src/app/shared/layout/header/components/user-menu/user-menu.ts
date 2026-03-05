import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

import { AuthService } from '../../../../../core/services/auth/auth.service';
import { OutsideClickDirective } from '../../../../directives/outside-click.directive';

@Component({
  selector: 'app-user-menu',
  standalone: true,
  imports: [CommonModule, RouterModule, OutsideClickDirective],
  templateUrl: './user-menu.html',
  styleUrl: './user-menu.scss'
})
export class UserMenu {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isOpen = signal(false);
  readonly session = this.authService.session;
  readonly displayName = computed(() => this.session()?.username ?? 'Guest User');

  toggle(): void {
    this.isOpen.update(value => !value);
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.isOpen.set(false);
      this.router.navigate(['/auth/login']);
    });
  }
}
