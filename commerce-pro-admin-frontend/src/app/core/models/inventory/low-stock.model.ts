// src/app/core/models/inventory/low-stock.model.ts
// Low stock alerts and reorder management models

import type { InventoryProduct, Supplier } from './product.model';
import type { Warehouse } from './warehouse/warehouse.model';

export type AlertSeverity = 'critical' | 'warning' | 'info';

export type SupplierRating = 'preferred' | 'approved' | 'conditional' | 'blocked';

export type PurchaseOrderStatus = 'draft' | 'sent' | 'acknowledged' | 'partial' | 'received' | 'cancelled';

export interface LowStockAlert {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  image?: string;
  category: string;
  brand: string;
  warehouseId: string;
  warehouseName: string;
  currentStock: number;
  reservedStock: number;
  availableStock: number;
  reorderPoint: number;
  reorderQuantity: number;
  maxStockLevel: number;
  stockStatus: string;
  severity: AlertSeverity;
  daysUntilStockout?: number;
  averageDailyUsage: number;
  lastPurchaseDate?: Date;
  suggestedOrderQuantity: number;
  suppliers: string[];
  isAcknowledged: boolean;
  acknowledgedBy?: string;
  acknowledgedAt?: Date;
  createdAt: Date;
  product: InventoryProduct;
  warehouse: Warehouse;
  suggestedSupplier?: Supplier;
  resolvedAt?: Date;
  isRead: boolean;
  estimatedCost: number;
}

export interface POItemSuggestion {
  productId: string;
  productName: string;
  sku: string;
  image?: string;
  suggestedQuantity: number;
  unitCost: number;
  totalCost: number;
  supplierId: string;
  supplierName: string;
  leadTime: number;
  reason: string;
  category?: string;
  urgency?: string;
  currentStock?: number;
  reorderPoint?: number;
  notes?: string;
}

export interface PurchaseOrderSuggestion {
  id: string;
  supplierId: string;
  supplierName: string;
  supplierRating: SupplierRating;
  items: POItemSuggestion[];
  totalItems: number;
  totalCost: number;
  currency: string;
  estimatedDeliveryDate: Date;
  priority: 'low' | 'normal' | 'high' | 'urgent';
  notes?: string;
  generatedAt: Date;
  generatedBy: string;
  supplier: Supplier;
  total: number;
  totalQuantity: number;
  consolidationOpportunities?: any;
  subtotal?: number;
  tax?: number;
  shipping?: number;
}

export interface ReorderRule {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  warehouseId: string;
  warehouseName: string;
  reorderPoint: number;
  reorderQuantity: number;
  maxStockLevel: number;
  minOrderQuantity?: number;
  orderMultiple?: number;
  leadTimeDays: number;
  safetyStockDays: number;
  isAutomatic: boolean;
  preferredSupplierId?: string;
  preferredSupplierName?: string;
  seasonalAdjustment?: number;
  growthFactor?: number;
  isActive: boolean;
  lastCalculatedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
  name: string;
  description?: string;
  category?: string;
  supplier?: string;
  warehouse?: string;
  reorderPointFormula: 'fixed' | 'dynamic' | 'forecast';
  reorderPointValue?: number;
  reorderPointDays?: number;
  reorderQtyFormula: 'eoq' | 'fixed' | 'max_minus_current';
  reorderQtyValue?: number;
  leadTimeBuffer: number;
  priority: number;
}

export type AlertChannel = 'email' | 'sms' | 'push' | 'dashboard' | 'webhook';

export interface AlertConfig {
  id: string;
  name: string;
  description?: string;
  severity: AlertSeverity;
  stockThresholdType: 'fixed' | 'percentage' | 'days_of_supply';
  thresholdValue: number;
  channels: AlertChannel[];
  recipients: string[];
  warehouseIds: string[];
  categoryIds?: string[];
  productIds?: string[];
  supplierIds?: string[];
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
  enabled?: Boolean;
}
