import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';

import { AuditLogEntry } from '../../../core/models/identity';
import { IdentityService } from '../../../core/services/identity/identity.service';

@Component({
  selector: 'app-identity-audit',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './audit.html',
  styleUrl: './audit.scss'
})
export class IdentityAudit implements OnInit {
  private readonly identityService = inject(IdentityService);

  readonly logs = signal<AuditLogEntry[]>([]);

  ngOnInit(): void {
    this.identityService.getAuditLogs(0, 30).subscribe(page => this.logs.set(page.content));
  }
}
