// src/app/core/services/inventory/low-stock.service.ts
// Low stock alerts service - Angular HTTP Client integration with Spring Boot backend

import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { ApiResponse } from '../../models/common';
import { 
  LowStockAlert, 
  LowStockFilter,
  LowStockStats,
  ReorderSuggestion 
} from '../../models/inventory';

// API Configuration
const API_CONFIG = {
  baseUrl: 'http://localhost:8080/api',
  endpoints: {
    lowStockAlerts: '/inventory/alerts/low-stock',
    criticalAlerts: '/inventory/alerts/critical',
    warehouseAlerts: '/inventory/alerts/warehouse',
    acknowledge: '/inventory/alerts',
    reorderSuggestions: '/inventory/reorder-suggestions'
  }
};

@Injectable({
  providedIn: 'root'
})
export class LowStockService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = API_CONFIG.baseUrl;
  
  // Private signals for state management
  private alerts = signal<LowStockAlert[]>([]);
  private loading = signal<boolean>(false);
  private error = signal<string | null>(null);
  
  // Public signals for PO suggestions
  readonly poSuggestions = signal<ReorderSuggestion[]>([]);
  
  // Public computed signals
  readonly allAlerts = computed(() => this.alerts());
  readonly isLoading = computed(() => this.loading());
  readonly hasError = computed(() => this.error());
  
  // Low stock stats computation
  readonly lowStockStats = computed<LowStockStats>(() => {
    const all = this.alerts();
    const criticalCount = all.filter(a => a.status === 'CRITICAL').length;
    const lowCount = all.filter(a => a.status === 'LOW').length;
    const reorderCount = all.filter(a => a.status === 'REORDER').length;
    const acknowledgedCount = all.filter(a => a.acknowledged).length;
    
    return {
      total: all.length,
      critical: criticalCount,
      low: lowCount,
      reorder: reorderCount,
      acknowledged: acknowledgedCount,
      unacknowledged: all.length - acknowledgedCount,
      unreadAlerts: all.filter(a => !a.isRead).length,
      totalItems: all.length,
      criticalCount,
      lowCount,
      adequateCount: 0,
      excessCount: 0,
      poSuggestions: this.poSuggestions().length,
      totalShortageValue: all.reduce((sum, a) => sum + ((a.reorderQuantity || 0) * 10), 0),
      avgDaysUntilStockout: all.length > 0 
        ? all.reduce((sum, a) => sum + (a.daysUntilStockout || 0), 0) / all.length 
        : 0
    };
  });
  
  // Critical alerts
  readonly criticalAlerts = computed(() => 
    this.alerts().filter(a => a.status === 'CRITICAL')
  );
  
  // Unacknowledged alerts
  readonly unacknowledgedAlerts = computed(() => 
    this.alerts().filter(a => !a.acknowledged && !a.resolved)
  );

  constructor() {
    this.loadData();
  }

  // ==================== Load Operations ====================

  /**
   * Load all low stock data
   */
  loadData(): void {
    this.loading.set(true);
    this.error.set(null);
    
    this.getLowStockAlerts().subscribe({
      next: (alerts) => {
        this.alerts.set(alerts);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
    
    // Load PO suggestions
    this.loadMockPOSuggestions();
  }
  
  private loadMockPOSuggestions() {
    const mockSuggestions: ReorderSuggestion[] = [
      {
        id: 'sugg-1',
        productId: '1',
        productName: 'Wireless Headphones',
        sku: 'ELEC-HP-001',
        currentStock: 5,
        suggestedQuantity: 50,
        unitCost: 50,
        totalCost: 2500,
        supplier: 'TechCorp',
        supplierName: 'TechCorp Inc.',
        leadTimeDays: 7,
        estimatedDeliveryDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
      },
      {
        id: 'sugg-2',
        productId: '2',
        productName: 'Bluetooth Speaker',
        sku: 'ELEC-BS-001',
        currentStock: 2,
        suggestedQuantity: 100,
        unitCost: 30,
        totalCost: 3000,
        supplier: 'AudioTech',
        supplierName: 'AudioTech Ltd.',
        leadTimeDays: 5,
        estimatedDeliveryDate: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000)
      }
    ];
    this.poSuggestions.set(mockSuggestions);
  }

  /**
   * Get all low stock alerts
   * GET /api/inventory/alerts/low-stock
   */
  getLowStockAlerts(filter?: LowStockFilter): Observable<LowStockAlert[]> {
    // For now, return mock data
    // In production: return this.http.get<ApiResponse<LowStockAlert[]>>(url).pipe(map(r => r.data))
    const mockAlerts: LowStockAlert[] = [
      {
        id: '1',
        inventoryItemId: 'inv-1',
        productId: 'prod-1',
        productName: 'Wireless Headphones',
        productSku: 'ELEC-HP-001',
        productImage: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=100',
        category: 'Electronics',
        warehouseId: '1',
        warehouseName: 'Main Warehouse',
        currentStock: 5,
        availableStock: 5,
        lowStockThreshold: 10,
        reorderPoint: 10,
        reorderQuantity: 50,
        status: 'LOW',
        severity: 'warning',
        daysUntilStockout: 5,
        avgDailyUsage: 1,
        acknowledged: false,
        resolved: false,
        isRead: false,
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        id: '2',
        inventoryItemId: 'inv-2',
        productId: 'prod-2',
        productName: 'Bluetooth Speaker',
        productSku: 'ELEC-BS-001',
        productImage: 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=100',
        category: 'Electronics',
        warehouseId: '1',
        warehouseName: 'Main Warehouse',
        currentStock: 2,
        availableStock: 2,
        lowStockThreshold: 15,
        reorderPoint: 15,
        reorderQuantity: 100,
        status: 'CRITICAL',
        severity: 'critical',
        daysUntilStockout: 2,
        avgDailyUsage: 1,
        acknowledged: false,
        resolved: false,
        isRead: false,
        createdAt: new Date(),
        updatedAt: new Date()
      },
      {
        id: '3',
        inventoryItemId: 'inv-3',
        productId: 'prod-3',
        productName: 'USB-C Cable',
        productSku: 'ELEC-CBL-001',
        category: 'Electronics',
        warehouseId: '2',
        warehouseName: 'West Coast Warehouse',
        currentStock: 8,
        availableStock: 8,
        lowStockThreshold: 20,
        reorderPoint: 20,
        reorderQuantity: 200,
        status: 'REORDER',
        severity: 'info',
        daysUntilStockout: 10,
        avgDailyUsage: 0.8,
        acknowledged: true,
        resolved: false,
        isRead: true,
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ];
    return of(mockAlerts);
  }

  // ==================== Alert Actions ====================

  /**
   * Acknowledge a low stock alert
   * PUT /api/inventory/alerts/{id}/acknowledge
   */
  acknowledgeAlert(alertId: string): Observable<void> {
    this.alerts.update(current => 
      current.map(a => 
        a.id === alertId 
          ? { ...a, acknowledged: true, isRead: true, updatedAt: new Date() }
          : a
      )
    );
    return of(void 0);
  }

  /**
   * Resolve a low stock alert
   * PUT /api/inventory/alerts/{id}/resolve
   */
  resolveAlert(alertId: string): Observable<void> {
    this.alerts.update(current => 
      current.map(a => 
        a.id === alertId 
          ? { ...a, resolved: true, resolvedAt: new Date(), updatedAt: new Date() }
          : a
      )
    );
    return of(void 0);
  }

  /**
   * Mark alert as read
   */
  markAsRead(alertId: string): Observable<void> {
    this.alerts.update(current => 
      current.map(a => 
        a.id === alertId 
          ? { ...a, isRead: true, updatedAt: new Date() }
          : a
      )
    );
    return of(void 0);
  }

  /**
   * Bulk acknowledge alerts
   */
  bulkAcknowledge(alertIds: string[]): Observable<void> {
    this.alerts.update(current => 
      current.map(a => 
        alertIds.includes(a.id)
          ? { ...a, acknowledged: true, isRead: true, updatedAt: new Date() }
          : a
      )
    );
    return of(void 0);
  }

  // ==================== Helper Methods ====================

  retry(): void {
    this.loadData();
  }
}
