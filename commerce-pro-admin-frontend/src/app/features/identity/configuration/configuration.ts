import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';

import { SuperAdminConfigView } from '../../../core/models/identity';
import { IdentityService } from '../../../core/services/identity/identity.service';

@Component({
  selector: 'app-identity-configuration',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './configuration.html',
  styleUrl: './configuration.scss'
})
export class IdentityConfiguration implements OnInit {
  private readonly identityService = inject(IdentityService);

  readonly config = signal<SuperAdminConfigView | null>(null);

  ngOnInit(): void {
    this.loadConfiguration();
  }

  loadConfiguration(): void {
    this.identityService.getSuperAdminConfiguration().subscribe(value => this.config.set(value));
  }

  reloadConfiguration(): void {
    this.identityService.reloadConfiguration().subscribe(success => {
      if (success) this.loadConfiguration();
    });
  }
}
