// src/app/core/services/inventory/demand-forecast.service.ts
// Demand forecasting service with API-ready patterns

import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, tap, delay } from 'rxjs/operators';
import { 
  DemandForecast, 
  ForecastStatus,
  ForecastPeriod,
  ForecastAlgorithm,
  ForecastAccuracy 
} from '../../models/inventory/demand-forecast.model';

export interface ForecastStats {
  totalForecasts: number;
  byStatus: Record<ForecastStatus, number>;
  byPeriod: Record<ForecastPeriod, number>;
  totalPredictedDemand: number;
  averageConfidence: number;
}

export interface GenerateForecastRequest {
  productId: string;
  warehouseId?: string;
  period: ForecastPeriod;
  algorithm: ForecastAlgorithm;
  startDate: Date;
  endDate: Date;
  historicalDays?: number;
}

@Injectable({
  providedIn: 'root'
})
export class DemandForecastService {
  // Base URL - easily switchable between JSON files and Spring Boot API
  private readonly BASE_URL = 'assets/data/inventory'; // For JSON files
  // private readonly BASE_URL = '/api/v1/inventory'; // For Spring Boot API
  
  private readonly FORECASTS_URL = `${this.BASE_URL}/demand-forecasts.json`;
  
  private http = inject(HttpClient);
  
  // Private signals for state management
  private forecasts = signal<DemandForecast[]>([]);
  private loading = signal<boolean>(false);
  private error = signal<string | null>(null);
  
  // Public computed signals
  readonly allForecasts = computed(() => this.forecasts());
  readonly isLoading = computed(() => this.loading());
  readonly hasError = computed(() => this.error());
  
  // Forecast stats computation
  readonly forecastStats = computed<ForecastStats>(() => {
    const all = this.forecasts();
    
    const byStatus = all.reduce((acc, f) => {
      acc[f.status] = (acc[f.status] || 0) + 1;
      return acc;
    }, {} as Record<ForecastStatus, number>);
    
    const byPeriod = all.reduce((acc, f) => {
      acc[f.period] = (acc[f.period] || 0) + 1;
      return acc;
    }, {} as Record<ForecastPeriod, number>);
    
    const totalPredicted = all.reduce((sum, f) => sum + f.totalPredictedDemand, 0);
    const avgConfidence = all.length > 0 
      ? all.reduce((sum, f) => {
          const forecastAvg = f.forecastData.reduce((s, d) => s + d.confidence, 0) / f.forecastData.length;
          return sum + forecastAvg;
        }, 0) / all.length 
      : 0;
    
    return {
      totalForecasts: all.length,
      byStatus,
      byPeriod,
      totalPredictedDemand: totalPredicted,
      averageConfidence: Math.round(avgConfidence * 100) / 100
    };
  });
  
  // Active forecasts only
  readonly activeForecasts = computed(() => 
    this.forecasts().filter(f => f.status === 'active')
  );
  
  // Forecast accuracy metrics
  readonly accuracy = computed(() => {
    const all = this.forecasts();
    if (all.length === 0) {
      return {
        mape: 0,
        mae: 0,
        rmse: 0,
        bias: 0,
        accuracyScore: 0,
        lastTested: new Date()
      };
    }
    
    // Calculate average metrics across all forecasts
    const avgConfidence = all.reduce((sum, f) => {
      const forecastAvg = f.forecastData.reduce((s, d) => s + d.confidence, 0) / f.forecastData.length;
      return sum + forecastAvg;
    }, 0) / all.length;
    
    return {
      mape: 5 + Math.random() * 10,
      mae: Math.round(all.reduce((sum, f) => sum + f.totalPredictedDemand, 0) * 0.05 / all.length),
      rmse: Math.round(all.reduce((sum, f) => sum + f.totalPredictedDemand, 0) * 0.08 / all.length),
      bias: 0,
      accuracyScore: Math.round(avgConfidence * 100),
      lastTested: new Date()
    };
  });
  
  // Forecasts by product
  readonly forecastsByProduct = computed(() => {
    const grouped: Record<string, DemandForecast[]> = {};
    this.forecasts().forEach(f => {
      if (!grouped[f.productId]) {
        grouped[f.productId] = [];
      }
      grouped[f.productId].push(f);
    });
    return grouped;
  });

  constructor() {
    this.loadForecasts();
  }

  // ==================== Load Operations ====================

  /**
   * Load all demand forecasts
   * For Spring Boot: GET /api/v1/inventory/forecasts
   */
  loadForecasts(): void {
    this.loading.set(true);
    this.error.set(null);
    
    this.http.get<DemandForecast[]>(this.FORECASTS_URL).pipe(
      delay(300),
      map(forecasts => this.transformDates(forecasts)),
      catchError(this.handleError('loadForecasts', []))
    ).subscribe({
      next: (forecasts) => {
        this.forecasts.set(forecasts);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }

  /**
   * Retry loading forecasts after error
   */
  retry(): void {
    this.loadForecasts();
  }

  /**
   * Refresh forecasts from server
   * For Spring Boot: POST /api/v1/inventory/forecasts/refresh
   */
  refreshForecasts(): Observable<DemandForecast[]> {
    this.loading.set(true);
    this.error.set(null);
    
    // For JSON files, just reload
    return this.http.get<DemandForecast[]>(this.FORECASTS_URL).pipe(
      delay(500),
      map(forecasts => this.transformDates(forecasts)),
      tap(forecasts => {
        this.forecasts.set(forecasts);
        this.loading.set(false);
      }),
      catchError(this.handleError('refreshForecasts', []))
    );
    
    // For Spring Boot API:
    // return this.http.post<DemandForecast[]>(`${this.BASE_URL}/forecasts/refresh`, {}).pipe(
    //   tap(forecasts => {
    //     this.forecasts.set(forecasts);
    //     this.loading.set(false);
    //   }),
    //   catchError(this.handleError('refreshForecasts', []))
    // );
  }

  // ==================== Read Operations ====================

  /**
   * Get all forecasts as observable
   * For Spring Boot: GET /api/v1/inventory/forecasts
   */
  getForecasts(): Observable<DemandForecast[]> {
    return this.http.get<DemandForecast[]>(this.FORECASTS_URL).pipe(
      map(forecasts => this.transformDates(forecasts)),
      catchError(this.handleError('getForecasts', []))
    );
  }

  /**
   * Get forecast by ID
   * For Spring Boot: GET /api/v1/inventory/forecasts/{id}
   */
  getForecastById(id: string): Observable<DemandForecast | null> {
    return this.http.get<DemandForecast[]>(this.FORECASTS_URL).pipe(
      map(forecasts => {
        const forecast = forecasts.find(f => f.id === id);
        return forecast ? this.transformDate(forecast) : null;
      }),
      catchError(this.handleError('getForecastById', null))
    );
    
    // For Spring Boot API:
    // return this.http.get<DemandForecast>(`${this.BASE_URL}/forecasts/${id}`).pipe(
    //   catchError(this.handleError('getForecastById', null))
    // );
  }

  /**
   * Get forecasts by product
   * For Spring Boot: GET /api/v1/inventory/forecasts?productId={id}
   */
  getForecastsByProduct(productId: string): Observable<DemandForecast[]> {
    return this.http.get<DemandForecast[]>(this.FORECASTS_URL).pipe(
      map(forecasts => forecasts.filter(f => f.productId === productId)),
      map(forecasts => this.transformDates(forecasts)),
      catchError(this.handleError('getForecastsByProduct', []))
    );
  }

  /**
   * Get forecasts by warehouse
   * For Spring Boot: GET /api/v1/inventory/forecasts?warehouseId={id}
   */
  getForecastsByWarehouse(warehouseId: string): Observable<DemandForecast[]> {
    return this.http.get<DemandForecast[]>(this.FORECASTS_URL).pipe(
      map(forecasts => forecasts.filter(f => f.warehouseId === warehouseId)),
      map(forecasts => this.transformDates(forecasts)),
      catchError(this.handleError('getForecastsByWarehouse', []))
    );
  }

  /**
   * Get forecasts by status
   * For Spring Boot: GET /api/v1/inventory/forecasts?status={status}
   */
  getForecastsByStatus(status: ForecastStatus): Observable<DemandForecast[]> {
    return this.http.get<DemandForecast[]>(this.FORECASTS_URL).pipe(
      map(forecasts => forecasts.filter(f => f.status === status)),
      map(forecasts => this.transformDates(forecasts)),
      catchError(this.handleError('getForecastsByStatus', []))
    );
  }

  // ==================== Generate Operations ====================

  /**
   * Generate new forecast
   * For Spring Boot: POST /api/v1/inventory/forecasts/generate
   */
  generateForecast(request: GenerateForecastRequest): Observable<DemandForecast> {
    // Generate mock forecast data
    const forecastData = this.generateMockForecastData(
      request.startDate, 
      request.endDate, 
      request.period
    );
    
    const totalPredicted = forecastData.reduce((sum, d) => sum + d.predictedDemand, 0);
    const avgConfidence = forecastData.reduce((sum, d) => sum + d.confidence, 0) / forecastData.length;
    
    const avgDailySales = Math.round(totalPredicted / forecastData.length * 100) / 100;
    
    const newForecast: DemandForecast = {
      id: this.generateId(),
      productId: request.productId,
      productName: 'Product ' + request.productId, // Would be fetched from product service
      sku: 'SKU-' + request.productId,
      warehouseId: request.warehouseId,
      warehouseName: request.warehouseId ? 'Warehouse ' + request.warehouseId : undefined,
      period: request.period,
      algorithm: request.algorithm,
      status: 'active',
      historicalData: [], // Would be populated from historical sales
      forecastData,
      startDate: request.startDate,
      endDate: request.endDate,
      generatedAt: new Date(),
      generatedBy: 'current-user', // Would come from auth service
      lastUpdatedAt: new Date(),
      totalPredictedDemand: totalPredicted,
      averageDailyDemand: avgDailySales,
      peakDemandDate: forecastData.reduce((max, d) => d.predictedDemand > max.predictedDemand ? d : max, forecastData[0])?.date,
      peakDemandQuantity: Math.max(...forecastData.map(d => d.predictedDemand)),
      safetyStockRecommendation: Math.round(totalPredicted * 0.2), // 20% safety stock
      reorderPointRecommendation: Math.round(totalPredicted * 0.15), // 15% reorder point
      product: {} as any, // Would be populated from product service
      avgDailySales: avgDailySales,
      trend: 'stable',
      alertLevel: 'none',
      forecasts: forecastData,
      forecastHorizon: forecastData.length,
      currentStock: 0
    };
    
    this.forecasts.update(current => [...current, newForecast]);
    
    return of(newForecast).pipe(delay(1000)); // Simulate ML processing delay
    
    // For Spring Boot API:
    // return this.http.post<DemandForecast>(`${this.BASE_URL}/forecasts/generate`, request).pipe(
    //   tap(() => this.loadForecasts()),
    //   catchError(this.handleError('generateForecast'))
    // );
  }

  /**
   * Archive forecast
   * For Spring Boot: POST /api/v1/inventory/forecasts/{id}/archive
   */
  archiveForecast(id: string): Observable<DemandForecast> {
    this.forecasts.update(current => 
      current.map(f => 
        f.id === id 
          ? { ...f, status: 'archived' as ForecastStatus, lastUpdatedAt: new Date() } 
          : f
      )
    );
    
    const updated = this.forecasts().find(f => f.id === id);
    return updated ? of(updated).pipe(delay(500)) : throwError(() => new Error('Forecast not found'));
  }

  /**
   * Mark forecast as obsolete
   * For Spring Boot: POST /api/v1/inventory/forecasts/{id}/obsolete
   */
  markObsolete(id: string): Observable<DemandForecast> {
    this.forecasts.update(current => 
      current.map(f => 
        f.id === id 
          ? { ...f, status: 'obsolete' as ForecastStatus, lastUpdatedAt: new Date() } 
          : f
      )
    );
    
    const updated = this.forecasts().find(f => f.id === id);
    return updated ? of(updated).pipe(delay(500)) : throwError(() => new Error('Forecast not found'));
  }

  /**
   * Get forecast accuracy metrics
   * For Spring Boot: GET /api/v1/inventory/forecasts/{id}/accuracy
   */
  getForecastAccuracy(forecastId: string): Observable<ForecastAccuracy | null> {
    // For JSON files, return mock accuracy data
    const forecast = this.forecasts().find(f => f.id === forecastId);
    if (!forecast) {
      return of(null);
    }
    
    const mockAccuracy: ForecastAccuracy = {
      forecastId,
      productId: forecast.productId,
      sku: forecast.sku,
      period: forecast.period,
      actualDemand: Math.round(forecast.totalPredictedDemand * (0.9 + Math.random() * 0.2)),
      predictedDemand: forecast.totalPredictedDemand,
      error: 0,
      errorPercentage: 0,
      mape: 5 + Math.random() * 10,
      mae: Math.round(forecast.totalPredictedDemand * 0.05),
      rmse: Math.round(forecast.totalPredictedDemand * 0.08),
      bias: Math.random() > 0.5 ? 1 : -1,
      accuracyScore: 85 + Math.random() * 10,
      evaluatedAt: new Date()
    };
    
    mockAccuracy.error = mockAccuracy.actualDemand - mockAccuracy.predictedDemand;
    mockAccuracy.errorPercentage = Math.abs(mockAccuracy.error / mockAccuracy.predictedDemand * 100);
    
    return of(mockAccuracy).pipe(delay(300));
    
    // For Spring Boot API:
    // return this.http.get<ForecastAccuracy>(`${this.BASE_URL}/forecasts/${forecastId}/accuracy`).pipe(
    //   catchError(this.handleError('getForecastAccuracy', null))
    // );
  }

  // ==================== Helper Methods ====================

  private generateMockForecastData(
    startDate: Date, 
    endDate: Date, 
    period: ForecastPeriod
  ) {
    const data = [];
    const current = new Date(startDate);
    const end = new Date(endDate);
    const baseDemand = 100 + Math.random() * 200;
    
    while (current <= end) {
      const seasonality = 1 + 0.3 * Math.sin(current.getMonth() / 12 * 2 * Math.PI);
      const trend = 1 + (current.getTime() - startDate.getTime()) / (endDate.getTime() - startDate.getTime()) * 0.1;
      const noise = 0.9 + Math.random() * 0.2;
      const predicted = Math.round(baseDemand * seasonality * trend * noise);
      
      data.push({
        period: current.toISOString().slice(0, 10),
        date: new Date(current),
        predictedDemand: predicted,
        lowerBound: Math.round(predicted * 0.8),
        upperBound: Math.round(predicted * 1.2),
        confidence: 0.7 + Math.random() * 0.25,
        seasonalityFactor: seasonality,
        trendFactor: trend
      });
      
      // Advance by period
      switch (period) {
        case 'daily':
          current.setDate(current.getDate() + 1);
          break;
        case 'weekly':
          current.setDate(current.getDate() + 7);
          break;
        case 'monthly':
          current.setMonth(current.getMonth() + 1);
          break;
        case 'quarterly':
          current.setMonth(current.getMonth() + 3);
          break;
        case 'yearly':
          current.setFullYear(current.getFullYear() + 1);
          break;
      }
    }
    
    return data;
  }

  private transformDates(forecasts: DemandForecast[]): DemandForecast[] {
    return forecasts.map(f => this.transformDate(f));
  }

  private transformDate(forecast: DemandForecast): DemandForecast {
    return {
      ...forecast,
      startDate: new Date(forecast.startDate),
      endDate: new Date(forecast.endDate),
      generatedAt: new Date(forecast.generatedAt),
      lastUpdatedAt: new Date(forecast.lastUpdatedAt),
      peakDemandDate: forecast.peakDemandDate ? new Date(forecast.peakDemandDate) : undefined,
      historicalData: forecast.historicalData?.map(h => ({
        ...h,
        date: new Date(h.date)
      })) || [],
      forecastData: forecast.forecastData?.map(f => ({
        ...f,
        date: new Date(f.date)
      })) || []
    };
  }

  private generateId(): string {
    return `frc_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`;
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      this.error.set(error.message);
      return of(result as T);
    };
  }
}
