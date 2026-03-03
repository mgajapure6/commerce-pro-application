// src/app/core/models/inventory/stock-adjustment.model.ts
// Stock adjustment and correction models

export type AdjustmentType = 'count' | 'damage' | 'receiving' | 'return' | 'transfer' | 'correction' | 'expiry';

export type AdjustmentStatus = 'draft' | 'pending' | 'approved' | 'rejected' | 'completed';

export type AdjustmentReason = 'damage' | 'expired' | 'lost' | 'found' | 'theft' | 'correction' | 'system_error' | 'other';

export interface AdjustmentLineItem {
  id: string;
  inventoryItemId: string;
  product: any;
  warehouse: any;
  binLocation?: string;
  systemQty: number;
  countedQty: number;
  adjustmentQuantity?: number;
  difference: number;
  unitCost: number;
  totalValue: number;
  totalValueImpact?: number;
  reason: AdjustmentReason;
  notes?: string;
}

export interface AdjustmentBatch {
  id: string;
  batchNumber?: string;
  reference?: string;
  type: AdjustmentType;
  status: AdjustmentStatus;
  warehouseId?: string;
  warehouse?: any;
  lineItems: AdjustmentLineItem[];
  totalItems?: number;
  totalDifference?: number;
  totalValueImpact?: number;
  createdBy?: string;
  approvedBy?: string;
  approvedAt?: Date;
  createdAt?: Date;
  updatedAt?: Date;
  notes?: string;
}

export interface StockAdjustment {
  id: string;
  adjustmentNumber: string;
  type: AdjustmentType;
  status: AdjustmentStatus;
  warehouseId: string;
  warehouseName: string;
  items: AdjustmentLineItem[];
  batches: AdjustmentBatch[];
  totalItems: number;
  totalValueImpact: number;
  requestedBy: string;
  approvedBy?: string;
  approvedAt?: Date;
  completedAt?: Date;
  rejectedBy?: string;
  rejectedAt?: Date;
  rejectionReason?: string;
  notes?: string;
  reference?: string;
  referenceType?: 'cycle_count' | 'audit' | 'system' | 'manual';
  createdAt: Date;
  updatedAt: Date;
}
