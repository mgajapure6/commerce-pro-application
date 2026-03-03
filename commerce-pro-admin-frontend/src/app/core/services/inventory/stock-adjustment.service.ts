// src/app/core/services/inventory/stock-adjustment.service.ts
// Stock Adjustment service - Angular HTTP Client integration with Spring Boot backend

import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { ApiResponse } from '../../models/common';
import { StockMovement } from '../../models/inventory';

// API Configuration
const API_CONFIG = {
  baseUrl: 'http://localhost:8080/api',
  endpoints: {
    inventory: '/inventory',
    adjustments: '/inventory/adjustments'
  }
};

export type AdjustmentType = 'count' | 'damage' | 'receiving' | 'return' | 'transfer' | 'correction' | 'expiry';
export type AdjustmentStatus = 'draft' | 'pending' | 'approved' | 'rejected' | 'completed';
export type AdjustmentReason = 'damage' | 'expired' | 'lost' | 'found' | 'theft' | 'correction' | 'system_error' | 'other';

export interface AdjustmentItem {
  id: string;
  inventoryItemId: string;
  productId: string;
  productName: string;
  sku: string;
  image?: string;
  systemQty: number;
  countedQty: number;
  difference: number;
  unitCost: number;
  totalValue: number;
  reason: AdjustmentReason;
  notes?: string;
}

export interface AdjustmentBatch {
  id: string;
  adjustmentNumber: string;
  type: AdjustmentType;
  status: AdjustmentStatus;
  warehouseId: string;
  warehouseName?: string;
  reference?: string;
  notes?: string;
  items: AdjustmentItem[];
  totalValue: number;
  createdBy?: string;
  approvedBy?: string;
  approvedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}

export interface AdjustmentStats {
  total: number;
  draft: number;
  pending: number;
  approved: number;
  completed: number;
  rejected: number;
  totalValue: number;
}

@Injectable({
  providedIn: 'root'
})
export class StockAdjustmentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = API_CONFIG.baseUrl;
  
  // Private signals for state management
  private adjustments = signal<AdjustmentBatch[]>([]);
  private loading = signal<boolean>(false);
  private error = signal<string | null>(null);
  
  // Public computed signals
  readonly allAdjustments = computed(() => this.adjustments());
  readonly isLoading = computed(() => this.loading());
  readonly hasError = computed(() => this.error());
  
  // Stats computation
  readonly stats = computed<AdjustmentStats>(() => {
    const all = this.adjustments();
    return {
      total: all.length,
      draft: all.filter(a => a.status === 'draft').length,
      pending: all.filter(a => a.status === 'pending').length,
      approved: all.filter(a => a.status === 'approved').length,
      completed: all.filter(a => a.status === 'completed').length,
      rejected: all.filter(a => a.status === 'rejected').length,
      totalValue: all.reduce((sum, a) => sum + (a.totalValue || 0), 0)
    };
  });

  constructor() {
    this.loadMockData();
  }

  private loadMockData() {
    const mockAdjustments: AdjustmentBatch[] = [
      {
        id: '1',
        adjustmentNumber: 'ADJ-2024-001',
        type: 'correction',
        status: 'completed',
        warehouseId: '1',
        warehouseName: 'Main Warehouse',
        reference: 'Annual Count',
        notes: 'Annual inventory count adjustment',
        items: [
          {
            id: 'item-1',
            inventoryItemId: 'inv-1',
            productId: 'prod-1',
            productName: 'Wireless Headphones',
            sku: 'ELEC-HP-001',
            image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=100',
            systemQty: 100,
            countedQty: 95,
            difference: -5,
            unitCost: 50,
            totalValue: 250,
            reason: 'correction',
            notes: 'Count discrepancy'
          }
        ],
        totalValue: 250,
        createdBy: 'System',
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        id: '2',
        adjustmentNumber: 'ADJ-2024-002',
        type: 'damage',
        status: 'pending',
        warehouseId: '1',
        warehouseName: 'Main Warehouse',
        reference: 'DAMAGE-001',
        notes: 'Damaged during shipping',
        items: [
          {
            id: 'item-2',
            inventoryItemId: 'inv-2',
            productId: 'prod-2',
            productName: 'Bluetooth Speaker',
            sku: 'ELEC-BS-001',
            image: 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=100',
            systemQty: 50,
            countedQty: 48,
            difference: -2,
            unitCost: 30,
            totalValue: 60,
            reason: 'damage',
            notes: 'Cracked casing'
          }
        ],
        totalValue: 60,
        createdBy: 'John Doe',
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        id: '3',
        adjustmentNumber: 'ADJ-2024-003',
        type: 'count',
        status: 'draft',
        warehouseId: '2',
        warehouseName: 'West Coast Warehouse',
        reference: 'CYCLE-Q1-2024',
        notes: 'Quarterly cycle count',
        items: [],
        totalValue: 0,
        createdBy: 'Jane Smith',
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ];
    this.adjustments.set(mockAdjustments);
  }

  // ==================== Adjustment Operations ====================

  /**
   * Create adjustment batch
   * POST /api/inventory/adjustments
   */
  createAdjustment(batch: Partial<AdjustmentBatch>): Observable<AdjustmentBatch> {
    const newBatch: AdjustmentBatch = {
      ...batch as AdjustmentBatch,
      id: `adj-${Date.now()}`,
      adjustmentNumber: `ADJ-${Date.now()}`,
      createdAt: new Date(),
      updatedAt: new Date()
    };
    
    this.adjustments.update(current => [newBatch, ...current]);
    return of(newBatch);
  }

  /**
   * Update adjustment batch
   * PUT /api/inventory/adjustments/{id}
   */
  updateAdjustment(id: string, updates: Partial<AdjustmentBatch>): Observable<AdjustmentBatch> {
    this.adjustments.update(current => 
      current.map(a => 
        a.id === id 
          ? { ...a, ...updates, updatedAt: new Date() }
          : a
      )
    );
    const updated = this.adjustments().find(a => a.id === id);
    return of(updated!);
  }

  /**
   * Apply stock adjustment for a single item
   * POST /api/inventory/{id}/stock
   */
  applyAdjustment(
    inventoryItemId: string, 
    newQuantity: number, 
    reason: string, 
    notes?: string
  ): Observable<StockMovement> {
    // In production, this would call the backend
    // For now, return mock response
    const mockMovement: StockMovement = {
      id: `mov-${Date.now()}`,
      inventoryId: inventoryItemId,
      productId: 'prod-1',
      warehouseId: '1',
      type: 'ADJUSTMENT',
      quantity: newQuantity,
      previousQuantity: 0,
      newQuantity: newQuantity,
      reason,
      notes,
      createdAt: new Date()
    };
    return of(mockMovement);
  }

  /**
   * Delete adjustment batch
   * DELETE /api/inventory/adjustments/{id}
   */
  deleteAdjustment(id: string): Observable<void> {
    this.adjustments.update(current => current.filter(a => a.id !== id));
    return of(void 0);
  }

  /**
   * Approve adjustment
   * PUT /api/inventory/adjustments/{id}/approve
   */
  approveAdjustment(id: string, approvedBy: string): Observable<AdjustmentBatch> {
    this.adjustments.update(current => 
      current.map(a => 
        a.id === id 
          ? { ...a, status: 'approved', approvedBy, approvedAt: new Date(), updatedAt: new Date() }
          : a
      )
    );
    const updated = this.adjustments().find(a => a.id === id);
    return of(updated!);
  }

  /**
   * Reject adjustment
   * PUT /api/inventory/adjustments/{id}/reject
   */
  rejectAdjustment(id: string, rejectedBy: string, reason: string): Observable<AdjustmentBatch> {
    this.adjustments.update(current => 
      current.map(a => 
        a.id === id 
          ? { ...a, status: 'rejected', updatedAt: new Date() }
          : a
      )
    );
    const updated = this.adjustments().find(a => a.id === id);
    return of(updated!);
  }

  /**
   * Complete adjustment
   * PUT /api/inventory/adjustments/{id}/complete
   */
  completeAdjustment(id: string): Observable<AdjustmentBatch> {
    this.adjustments.update(current => 
      current.map(a => 
        a.id === id 
          ? { ...a, status: 'completed', updatedAt: new Date() }
          : a
      )
    );
    const updated = this.adjustments().find(a => a.id === id);
    return of(updated!);
  }

  /**
   * Submit draft adjustment for approval
   */
  submitForApproval(id: string): Observable<AdjustmentBatch> {
    this.adjustments.update(current => 
      current.map(a => 
        a.id === id 
          ? { ...a, status: 'pending', updatedAt: new Date() }
          : a
      )
    );
    const updated = this.adjustments().find(a => a.id === id);
    return of(updated!);
  }

  // ==================== Helper Methods ====================

  retry(): void {
    this.loadMockData();
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed:`, error);
      this.error.set(error?.error?.message || error?.message || 'An error occurred');
      return of(result as T);
    };
  }
}
