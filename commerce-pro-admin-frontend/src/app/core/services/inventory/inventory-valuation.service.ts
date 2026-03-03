// src/app/core/services/inventory/inventory-valuation.service.ts
// Inventory valuation service with API-ready patterns

import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, tap, delay } from 'rxjs/operators';
import { 
  InventoryValuation, 
  ValuationMethodType,
  ValuationStatus,
  ValuationSummary,
  ValuationMethodConfig 
} from '../../models/inventory/inventory-valuation.model';

export interface ValuationStats {
  totalValuations: number;
  byMethod: Record<ValuationMethodType, number>;
  byStatus: Record<ValuationStatus, number>;
  totalValue: number;
  totalQuantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryValuationService {
  // Base URL - easily switchable between JSON files and Spring Boot API
  private readonly BASE_URL = 'assets/data/inventory'; // For JSON files
  // private readonly BASE_URL = '/api/v1/inventory'; // For Spring Boot API
  
  private readonly VALUATIONS_URL = `${this.BASE_URL}/inventory-valuations.json`;
  
  private http = inject(HttpClient);
  
  // Private signals for state management
  private valuations = signal<InventoryValuation[]>([]);
  private loading = signal<boolean>(false);
  private error = signal<string | null>(null);
  
  // Public computed signals
  readonly allValuations = computed(() => this.valuations());
  readonly isLoading = computed(() => this.loading());
  readonly hasError = computed(() => this.error());
  
  // Valuation stats computation
  readonly valuationStats = computed<ValuationStats>(() => {
    const all = this.valuations();
    
    const byMethod = all.reduce((acc, v) => {
      acc[v.valuationMethod] = (acc[v.valuationMethod] || 0) + 1;
      return acc;
    }, {
      fifo: 0,
      lifo: 0,
      weighted_average: 0,
      specific_identification: 0
    } as Record<ValuationMethodType, number>);
    
    const byStatus = all.reduce((acc, v) => {
      acc[v.status] = (acc[v.status] || 0) + 1;
      return acc;
    }, {} as Record<ValuationStatus, number>);
    
    return {
      totalValuations: all.length,
      byMethod,
      byStatus,
      totalValue: all.reduce((sum, v) => sum + v.totalValue, 0),
      totalQuantity: all.reduce((sum, v) => sum + v.totalQuantity, 0)
    };
  });
  
  // Valuation summary computation
  readonly valuationSummary = computed<ValuationSummary>(() => {
    const stats = this.valuationStats();
    const all = this.valuations();
    
    // Method breakdown
    const methodBreakdown: Record<ValuationMethodType, { count: number; value: number }> = {
      fifo: { count: 0, value: 0 },
      lifo: { count: 0, value: 0 },
      weighted_average: { count: 0, value: 0 },
      specific_identification: { count: 0, value: 0 }
    };
    
    all.forEach(v => {
      methodBreakdown[v.valuationMethod].count++;
      methodBreakdown[v.valuationMethod].value += v.totalValue;
    });
    
    // Warehouse breakdown
    const warehouseBreakdown: Record<string, { name: string; value: number; quantity: number }> = {};
    all.forEach(v => {
      const whId = v.warehouseId || 'unspecified';
      if (!warehouseBreakdown[whId]) {
        warehouseBreakdown[whId] = {
          name: v.warehouseName || 'Unspecified',
          value: 0,
          quantity: 0
        };
      }
      warehouseBreakdown[whId].value += v.totalValue;
      warehouseBreakdown[whId].quantity += v.totalQuantity;
    });
    
    return {
      totalInventoryValue: stats.totalValue,
      totalQuantity: stats.totalQuantity,
      averageUnitCost: stats.totalQuantity > 0 ? stats.totalValue / stats.totalQuantity : 0,
      methodBreakdown,
      warehouseBreakdown,
      categoryBreakdown: {}, // Would be populated from product category data
      generatedAt: new Date()
    };
  });
  
  // Active valuations only
  readonly activeValuations = computed(() => 
    this.valuations().filter(v => v.status === 'active')
  );

  constructor() {
    this.loadValuations();
  }

  // ==================== Load Operations ====================

  /**
   * Load all inventory valuations
   * For Spring Boot: GET /api/v1/inventory/valuations
   */
  loadValuations(): void {
    this.loading.set(true);
    this.error.set(null);
    
    this.http.get<InventoryValuation[]>(this.VALUATIONS_URL).pipe(
      delay(300),
      map(valuations => this.transformDates(valuations)),
      catchError(this.handleError('loadValuations', []))
    ).subscribe({
      next: (valuations) => {
        this.valuations.set(valuations);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  /**
   * Retry loading valuations after error
   */
  retry(): void {
    this.loadValuations();
  }

  // ==================== Read Operations ====================

  /**
   * Get all valuations as observable
   * For Spring Boot: GET /api/v1/inventory/valuations
   */
  getValuations(): Observable<InventoryValuation[]> {
    return this.http.get<InventoryValuation[]>(this.VALUATIONS_URL).pipe(
      map(valuations => this.transformDates(valuations)),
      catchError(this.handleError('getValuations', []))
    );
  }

  /**
   * Get valuation by ID
   * For Spring Boot: GET /api/v1/inventory/valuations/{id}
   */
  getValuationById(id: string): Observable<InventoryValuation | null> {
    return this.http.get<InventoryValuation[]>(this.VALUATIONS_URL).pipe(
      map(valuations => {
        const valuation = valuations.find(v => v.id === id);
        return valuation ? this.transformDate(valuation) : null;
      }),
      catchError(this.handleError('getValuationById', null))
    );
    
    // For Spring Boot API:
    // return this.http.get<InventoryValuation>(`${this.BASE_URL}/valuations/${id}`).pipe(
    //   catchError(this.handleError('getValuationById', null))
    // );
  }

  /**
   * Get valuation by product ID
   * For Spring Boot: GET /api/v1/inventory/valuations?productId={id}
   */
  getValuationByProduct(productId: string): Observable<InventoryValuation | null> {
    return this.http.get<InventoryValuation[]>(this.VALUATIONS_URL).pipe(
      map(valuations => {
        const valuation = valuations.find(v => v.productId === productId);
        return valuation ? this.transformDate(valuation) : null;
      }),
      catchError(this.handleError('getValuationByProduct', null))
    );
  }

  /**
   * Get valuations by warehouse
   * For Spring Boot: GET /api/v1/inventory/valuations?warehouseId={id}
   */
  getValuationsByWarehouse(warehouseId: string): Observable<InventoryValuation[]> {
    return this.http.get<InventoryValuation[]>(this.VALUATIONS_URL).pipe(
      map(valuations => valuations.filter(v => v.warehouseId === warehouseId)),
      map(valuations => this.transformDates(valuations)),
      catchError(this.handleError('getValuationsByWarehouse', []))
    );
  }

  /**
   * Get valuations by method
   * For Spring Boot: GET /api/v1/inventory/valuations?method={method}
   */
  getValuationsByMethod(method: ValuationMethodType): Observable<InventoryValuation[]> {
    return this.http.get<InventoryValuation[]>(this.VALUATIONS_URL).pipe(
      map(valuations => valuations.filter(v => v.valuationMethod === method)),
      map(valuations => this.transformDates(valuations)),
      catchError(this.handleError('getValuationsByMethod', []))
    );
  }

  // ==================== CRUD Operations ====================

  /**
   * Create new valuation
   * For Spring Boot: POST /api/v1/inventory/valuations
   */
  createValuation(valuation: Partial<InventoryValuation>): Observable<InventoryValuation> {
    const now = new Date();
    const newValuation: InventoryValuation = {
      ...valuation as InventoryValuation,
      id: this.generateId(),
      status: 'active',
      costLayers: valuation.costLayers || [],
      totalQuantity: valuation.totalQuantity || 0,
      averageUnitCost: valuation.averageUnitCost || 0,
      totalValue: valuation.totalValue || 0,
      calculatedAt: now,
      calculatedBy: 'current-user', // Would come from auth service
      createdAt: now
    };
    
    this.valuations.update(current => [...current, newValuation]);
    
    return of(newValuation).pipe(delay(500));
    
    // For Spring Boot API:
    // return this.http.post<InventoryValuation>(`${this.BASE_URL}/valuations`, valuation).pipe(
    //   tap(() => this.loadValuations()),
    //   catchError(this.handleError('createValuation'))
    // );
  }

  /**
   * Update valuation
   * For Spring Boot: PUT /api/v1/inventory/valuations/{id}
   */
  updateValuation(id: string, updates: Partial<InventoryValuation>): Observable<InventoryValuation> {
    this.valuations.update(current => 
      current.map(v => 
        v.id === id 
          ? { 
              ...v, 
              ...updates,
              lastAdjustmentAt: new Date(),
              createdAt: v.createdAt // Preserve original creation date
            } 
          : v
      )
    );
    
    const updated = this.valuations().find(v => v.id === id);
    return updated ? of(updated).pipe(delay(500)) : throwError(() => new Error('Valuation not found'));
    
    // For Spring Boot API:
    // return this.http.put<InventoryValuation>(`${this.BASE_URL}/valuations/${id}`, updates).pipe(
    //   tap(() => this.loadValuations()),
    //   catchError(this.handleError('updateValuation'))
    // );
  }

  /**
   * Delete valuation
   * For Spring Boot: DELETE /api/v1/inventory/valuations/{id}
   */
  deleteValuation(id: string): Observable<void> {
    this.valuations.update(current => current.filter(v => v.id !== id));
    return of(void 0).pipe(delay(500));
    
    // For Spring Boot API:
    // return this.http.delete<void>(`${this.BASE_URL}/valuations/${id}`).pipe(
    //   tap(() => this.loadValuations()),
    //   catchError(this.handleError('deleteValuation'))
    // );
  }

  // ==================== Valuation Operations ====================

  /**
   * Recalculate valuations
   * For Spring Boot: POST /api/v1/inventory/valuations/recalculate
   */
  recalculateValuations(
    productIds?: string[], 
    method?: ValuationMethodType
  ): Observable<InventoryValuation[]> {
    this.loading.set(true);
    
    // Simulate recalculation
    const valuationsToUpdate = productIds 
      ? this.valuations().filter(v => productIds.includes(v.productId))
      : this.valuations();
    
    const updatedValuations = valuationsToUpdate.map(v => {
      const newMethod = method || v.valuationMethod;
      const recalculatedValue = this.simulateRecalculation(v, newMethod);
      
      return {
        ...v,
        valuationMethod: newMethod,
        totalValue: recalculatedValue,
        averageUnitCost: v.totalQuantity > 0 ? recalculatedValue / v.totalQuantity : 0,
        calculatedAt: new Date(),
        lastAdjustmentAt: new Date()
      };
    });
    
    // Update state
    this.valuations.update(current => 
      current.map(v => {
        const updated = updatedValuations.find(u => u.id === v.id);
        return updated || v;
      })
    );
    
    this.loading.set(false);
    
    return of(updatedValuations).pipe(delay(1000));
    
    // For Spring Boot API:
    // return this.http.post<InventoryValuation[]>(`${this.BASE_URL}/valuations/recalculate`, {
    //   productIds,
    //   method
    // }).pipe(
    //   tap(valuations => this.valuations.set(valuations)),
    //   tap(() => this.loading.set(false)),
    //   catchError(this.handleError('recalculateValuations', []))
    // );
  }

  /**
   * Get valuation summary report
   * For Spring Boot: GET /api/v1/inventory/valuations/summary
   */
  getValuationSummary(): Observable<ValuationSummary> {
    return of(this.valuationSummary()).pipe(delay(300));
    
    // For Spring Boot API:
    // return this.http.get<ValuationSummary>(`${this.BASE_URL}/valuations/summary`).pipe(
    //   catchError(this.handleError('getValuationSummary'))
    // );
  }

  /**
   * Change valuation method
   * For Spring Boot: POST /api/v1/inventory/valuations/{id}/change-method
   */
  changeValuationMethod(id: string, method: ValuationMethodType): Observable<InventoryValuation> {
    const valuation = this.valuations().find(v => v.id === id);
    if (!valuation) {
      return throwError(() => new Error('Valuation not found'));
    }
    
    const recalculatedValue = this.simulateRecalculation(valuation, method);
    
    return this.updateValuation(id, {
      valuationMethod: method,
      totalValue: recalculatedValue,
      averageUnitCost: valuation.totalQuantity > 0 ? recalculatedValue / valuation.totalQuantity : 0
    });
  }

  // ==================== Method Config Operations ====================

  /**
   * Get valuation method configurations
   * For Spring Boot: GET /api/v1/inventory/valuations/method-configs
   */
  getMethodConfigs(): Observable<ValuationMethodConfig[]> {
    // For JSON files, return empty array (configs not stored in JSON)
    return of([]).pipe(delay(200));
    
    // For Spring Boot API:
    // return this.http.get<ValuationMethodConfig[]>(`${this.BASE_URL}/valuations/method-configs`).pipe(
    //   catchError(this.handleError('getMethodConfigs', []))
    // );
  }

  /**
   * Create method configuration
   * For Spring Boot: POST /api/v1/inventory/valuations/method-configs
   */
  createMethodConfig(config: Partial<ValuationMethodConfig>): Observable<ValuationMethodConfig> {
    const newConfig: ValuationMethodConfig = {
      ...config as ValuationMethodConfig,
      id: this.generateId(),
      createdAt: new Date(),
      updatedAt: new Date()
    };
    
    return of(newConfig).pipe(delay(500));
  }

  // ==================== Helper Methods ====================

  private simulateRecalculation(valuation: InventoryValuation, method: ValuationMethodType): number {
    // Simulate different valuation methods
    switch (method) {
      case 'fifo':
        // FIFO: use oldest costs first
        return valuation.costLayers
          .sort((a, b) => new Date(a.receiptDate).getTime() - new Date(b.receiptDate).getTime())
          .reduce((sum, layer) => sum + (layer.unitCost * layer.remainingQuantity), 0);
      
      case 'lifo':
        // LIFO: use newest costs first
        return valuation.costLayers
          .sort((a, b) => new Date(b.receiptDate).getTime() - new Date(a.receiptDate).getTime())
          .reduce((sum, layer) => sum + (layer.unitCost * layer.remainingQuantity), 0);
      
      case 'weighted_average':
        // Weighted average
        const totalCost = valuation.costLayers.reduce((sum, l) => sum + l.totalCost, 0);
        const totalQty = valuation.costLayers.reduce((sum, l) => sum + l.remainingQuantity, 0);
        return totalQty > 0 ? (totalCost / totalQty) * valuation.totalQuantity : 0;
      
      case 'specific_identification':
        // Specific identification - return current value
      default:
        return valuation.totalValue;
    }
  }

  private transformDates(valuations: InventoryValuation[]): InventoryValuation[] {
    return valuations.map(v => this.transformDate(v));
  }

  private transformDate(valuation: InventoryValuation): InventoryValuation {
    return {
      ...valuation,
      calculatedAt: new Date(valuation.calculatedAt),
      lastAdjustmentAt: valuation.lastAdjustmentAt ? new Date(valuation.lastAdjustmentAt) : undefined,
      oldestCostLayerDate: valuation.oldestCostLayerDate ? new Date(valuation.oldestCostLayerDate) : undefined,
      newestCostLayerDate: valuation.newestCostLayerDate ? new Date(valuation.newestCostLayerDate) : undefined,
      costLayers: valuation.costLayers?.map(l => ({
        ...l,
        receiptDate: new Date(l.receiptDate),
        expiryDate: l.expiryDate ? new Date(l.expiryDate) : undefined
      })) || []
    };
  }

  private generateId(): string {
    return `val_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`;
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      this.error.set(error.message);
      return of(result as T);
    };
  }
}
