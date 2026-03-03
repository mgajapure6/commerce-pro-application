// src/app/core/services/inventory/stock-transfer.service.ts
// Stock Transfer service - Angular HTTP Client integration with Spring Boot backend

import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { ApiResponse } from '../../models/common';
import { StockTransferRequest } from '../../models/inventory';

// API Configuration
const API_CONFIG = {
  baseUrl: 'http://localhost:8080/api',
  endpoints: {
    transfers: '/inventory/transfers',
    transfer: '/inventory/transfer'
  }
};

export type TransferStatus = 'draft' | 'pending' | 'approved' | 'shipped' | 'in_transit' | 'received' | 'completed' | 'cancelled';
export type TransferPriority = 'low' | 'normal' | 'high' | 'urgent';
export type TransferType = 'standard' | 'replenishment' | 'returns' | 'consignment';

export interface TransferItem {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  image?: string;
  quantity: number;
  unitCost: number;
  totalValue: number;
  notes?: string;
}

export interface StockTransfer {
  id: string;
  transferNumber: string;
  type: TransferType;
  status: TransferStatus;
  priority: TransferPriority;
  fromWarehouseId: string;
  fromWarehouseName?: string;
  toWarehouseId: string;
  toWarehouseName?: string;
  reference?: string;
  notes?: string;
  items: TransferItem[];
  totalValue: number;
  createdBy?: string;
  approvedBy?: string;
  approvedAt?: Date;
  shippedAt?: Date;
  receivedAt?: Date;
  completedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}

export interface TransferStats {
  total: number;
  draft: number;
  pending: number;
  approved: number;
  inTransit: number;
  completed: number;
  cancelled: number;
  totalValue: number;
}

@Injectable({
  providedIn: 'root'
})
export class StockTransferService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = API_CONFIG.baseUrl;
  
  // Private signals for state management
  private transfers = signal<StockTransfer[]>([]);
  private loading = signal<boolean>(false);
  private error = signal<string | null>(null);
  
  // Public computed signals
  readonly allTransfers = computed(() => this.transfers());
  readonly isLoading = computed(() => this.loading());
  readonly hasError = computed(() => this.error());
  
  // Stats computation
  readonly stats = computed<TransferStats>(() => {
    const all = this.transfers();
    return {
      total: all.length,
      draft: all.filter(t => t.status === 'draft').length,
      pending: all.filter(t => t.status === 'pending').length,
      approved: all.filter(t => t.status === 'approved').length,
      inTransit: all.filter(t => t.status === 'in_transit' || t.status === 'shipped').length,
      completed: all.filter(t => t.status === 'completed').length,
      cancelled: all.filter(t => t.status === 'cancelled').length,
      totalValue: all.reduce((sum, t) => sum + (t.totalValue || 0), 0)
    };
  });

  constructor() {
    this.loadMockData();
  }

  private loadMockData() {
    const mockTransfers: StockTransfer[] = [
      {
        id: '1',
        transferNumber: 'TRF-2024-001',
        type: 'replenishment',
        status: 'completed',
        priority: 'normal',
        fromWarehouseId: '1',
        fromWarehouseName: 'Main Warehouse (NYC)',
        toWarehouseId: '2',
        toWarehouseName: 'West Coast Warehouse (LA)',
        reference: 'Monthly Replenishment',
        notes: 'Monthly replenishment for West Coast operations',
        items: [
          {
            id: 'item-1',
            productId: 'prod-1',
            productName: 'Wireless Headphones',
            sku: 'ELEC-HP-001',
            image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=100',
            quantity: 50,
            unitCost: 50,
            totalValue: 2500
          },
          {
            id: 'item-2',
            productId: 'prod-2',
            productName: 'Bluetooth Speaker',
            sku: 'ELEC-BS-001',
            image: 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=100',
            quantity: 30,
            unitCost: 30,
            totalValue: 900
          }
        ],
        totalValue: 3400,
        createdBy: 'John Manager',
        approvedBy: 'Jane Director',
        approvedAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
        shippedAt: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000),
        receivedAt: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000),
        completedAt: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000),
        createdAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000),
        updatedAt: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000)
      },
      {
        id: '2',
        transferNumber: 'TRF-2024-002',
        type: 'standard',
        status: 'in_transit',
        priority: 'high',
        fromWarehouseId: '1',
        fromWarehouseName: 'Main Warehouse (NYC)',
        toWarehouseId: '2',
        toWarehouseName: 'West Coast Warehouse (LA)',
        reference: 'URGENT-001',
        notes: 'Urgent transfer for out-of-stock items',
        items: [
          {
            id: 'item-3',
            productId: 'prod-3',
            productName: 'USB-C Cable',
            sku: 'ELEC-CBL-001',
            image: 'https://images.unsplash.com/photo-1625153669622-77066d97b0b0?w=100',
            quantity: 200,
            unitCost: 5,
            totalValue: 1000
          }
        ],
        totalValue: 1000,
        createdBy: 'Sarah Supervisor',
        approvedBy: 'John Manager',
        approvedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
        shippedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
        createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000),
        updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
      },
      {
        id: '3',
        transferNumber: 'TRF-2024-003',
        type: 'returns',
        status: 'pending',
        priority: 'low',
        fromWarehouseId: '2',
        fromWarehouseName: 'West Coast Warehouse (LA)',
        toWarehouseId: '1',
        toWarehouseName: 'Main Warehouse (NYC)',
        reference: 'RETURN-001',
        notes: 'Return damaged items to main warehouse',
        items: [
          {
            id: 'item-4',
            productId: 'prod-4',
            productName: 'Mechanical Keyboard',
            sku: 'ELEC-KB-001',
            image: 'https://images.unsplash.com/photo-1595225476474-87563907a212?w=100',
            quantity: 5,
            unitCost: 80,
            totalValue: 400
          }
        ],
        totalValue: 400,
        createdBy: 'Mike Warehouse',
        createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
        updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
      },
      {
        id: '4',
        transferNumber: 'TRF-2024-004',
        type: 'standard',
        status: 'draft',
        priority: 'normal',
        fromWarehouseId: '1',
        fromWarehouseName: 'Main Warehouse (NYC)',
        toWarehouseId: '2',
        toWarehouseName: 'West Coast Warehouse (LA)',
        reference: 'QUARTERLY-001',
        notes: 'Quarterly stock balancing',
        items: [],
        totalValue: 0,
        createdBy: 'John Manager',
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ];
    this.transfers.set(mockTransfers);
  }

  // ==================== Transfer Operations ====================

  /**
   * Create transfer
   * POST /api/inventory/transfers
   */
  createTransfer(transfer: Partial<StockTransfer>): Observable<StockTransfer> {
    const newTransfer: StockTransfer = {
      ...transfer as StockTransfer,
      id: `trf-${Date.now()}`,
      transferNumber: `TRF-${Date.now()}`,
      createdAt: new Date(),
      updatedAt: new Date()
    };
    
    this.transfers.update(current => [newTransfer, ...current]);
    return of(newTransfer);
  }

  /**
   * Update transfer
   * PUT /api/inventory/transfers/{id}
   */
  updateTransfer(id: string, updates: Partial<StockTransfer>): Observable<StockTransfer> {
    this.transfers.update(current => 
      current.map(t => 
        t.id === id 
          ? { ...t, ...updates, updatedAt: new Date() }
          : t
      )
    );
    const updated = this.transfers().find(t => t.id === id);
    return of(updated!);
  }

  /**
   * Update transfer status
   * PUT /api/inventory/transfers/{id}/status
   */
  updateStatus(id: string, status: TransferStatus): Observable<StockTransfer> {
    const updates: Partial<StockTransfer> = { status };
    
    // Add timestamp based on status
    if (status === 'approved') updates.approvedAt = new Date();
    if (status === 'shipped') updates.shippedAt = new Date();
    if (status === 'received') updates.receivedAt = new Date();
    if (status === 'completed') updates.completedAt = new Date();
    
    return this.updateTransfer(id, updates);
  }

  /**
   * Delete transfer
   * DELETE /api/inventory/transfers/{id}
   */
  deleteTransfer(id: string): Observable<void> {
    this.transfers.update(current => current.filter(t => t.id !== id));
    return of(void 0);
  }

  /**
   * Approve transfer
   * PUT /api/inventory/transfers/{id}/approve
   */
  approveTransfer(id: string, approvedBy: string): Observable<StockTransfer> {
    return this.updateStatus(id, 'approved');
  }

  /**
   * Cancel transfer
   * PUT /api/inventory/transfers/{id}/cancel
   */
  cancelTransfer(id: string): Observable<StockTransfer> {
    return this.updateStatus(id, 'cancelled');
  }

  /**
   * Ship transfer
   * PUT /api/inventory/transfers/{id}/ship
   */
  shipTransfer(id: string): Observable<StockTransfer> {
    return this.updateStatus(id, 'shipped');
  }

  /**
   * Mark as in transit
   * PUT /api/inventory/transfers/{id}/in-transit
   */
  markInTransit(id: string): Observable<StockTransfer> {
    return this.updateStatus(id, 'in_transit');
  }

  /**
   * Receive transfer
   * PUT /api/inventory/transfers/{id}/receive
   */
  receiveTransfer(id: string): Observable<StockTransfer> {
    return this.updateStatus(id, 'received');
  }

  /**
   * Complete transfer
   * PUT /api/inventory/transfers/{id}/complete
   */
  completeTransfer(id: string): Observable<StockTransfer> {
    return this.updateStatus(id, 'completed');
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
