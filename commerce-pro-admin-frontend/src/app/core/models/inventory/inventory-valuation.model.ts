// src/app/core/models/inventory/inventory-valuation.model.ts
// Inventory valuation and costing models

export type ValuationMethodType = 'fifo' | 'lifo' | 'weighted_average' | 'specific_identification';

export type ValuationStatus = 'active' | 'pending_review' | 'deprecated';

export interface CostLayer {
  id: string;
  productId: string;
  quantity: number;
  unitCost: number;
  totalCost: number;
  receiptDate: Date;
  purchaseOrderId?: string;
  supplierId?: string;
  batchNumber?: string;
  expiryDate?: Date;
  remainingQuantity: number;
}

export interface InventoryValuation {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  warehouseId?: string;
  warehouseName?: string;
  valuationMethod: ValuationMethodType;
  status: 'active' | 'pending_review' | 'deprecated';
  costLayers: CostLayer[];
  totalQuantity: number;
  averageUnitCost: number;
  totalValue: number;
  oldestCostLayerDate?: Date;
  newestCostLayerDate?: Date;
  calculatedAt: Date;
  calculatedBy: string;
  lastAdjustmentAt?: Date;
  notes?: string;
  createdAt?: Date;
}

export interface ValuationSummary {
  totalInventoryValue: number;
  totalQuantity: number;
  averageUnitCost: number;
  methodBreakdown: Record<ValuationMethodType, {
    count: number;
    value: number;
  }>;
  warehouseBreakdown: Record<string, {
    name: string;
    value: number;
    quantity: number;
  }>;
  categoryBreakdown: Record<string, {
    name: string;
    value: number;
    quantity: number;
  }>;
  generatedAt: Date;
}

export interface ValuationMethod {
  method?: string;
  name?: string;
  description?: string;
  icon?: string;
  color?: string;
  taxImplication?: string;
  bestFor?: string[];
}

export interface ValuationMethodConfig {
  id: string;
  productId?: string;
  categoryId?: string;
  defaultMethod: ValuationMethod;
  isProductSpecific: boolean;
  isCategoryDefault: boolean;
  isGlobalDefault: boolean;
  autoAdjustOnReceipt: boolean;
  autoAdjustOnSale: boolean;
  effectiveFrom: Date;
  effectiveTo?: Date;
  createdBy: string;
  createdAt: Date;
  updatedAt: Date;
}
