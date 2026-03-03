// Inventory Item model - matches backend InventoryDTO
import type { Warehouse } from './warehouse/warehouse.model';
import type { ProductSummary } from '../catalog/product.model';

export type InventoryItemStatus = 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK' | 'OVERSTOCK' | 'NOT_TRACKED';

export interface InventoryItem {
  id: string;
  productId: string;
  warehouseId: string;
  product?: ProductSummary;
  warehouse?: Warehouse;
  
  quantity: number;
  reserved: number;
  available: number;
  incoming: number;
  
  lowStockThreshold?: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  maxStockLevel?: number;
  safetyStock?: number;
  
  unitCost?: number;
  totalValue?: number;
  
  binLocation?: string;
  aisle?: string;
  zone?: string;
  
  trackInventory?: boolean;
  status?: InventoryItemStatus;
  
  lastRestocked?: Date;
  lastCounted?: Date;
  
  createdAt?: Date;
  updatedAt?: Date;
}

// Alias for compatibility
export type InventoryItemDTO = InventoryItem;

export interface InventoryRequest {
  productId: string;
  warehouseId: string;
  quantity: number;
  reserved?: number;
  lowStockThreshold?: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  maxStockLevel?: number;
  safetyStock?: number;
  unitCost?: number;
  binLocation?: string;
  aisle?: string;
  zone?: string;
  trackInventory?: boolean;
}

// Aliases for compatibility
export type CreateInventoryRequest = InventoryRequest;
export type UpdateInventoryRequest = InventoryRequest;

export interface InventoryFilter {
  searchQuery?: string;
  warehouseId?: string;
  status?: string;
  category?: string;
  productId?: string;
  lowStockOnly?: boolean;
  outOfStockOnly?: boolean;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// Additional types for inventory
export interface InventorySummary {
  totalItems: number;
  totalValue: number;
  lowStockCount: number;
  outOfStockCount: number;
}

export interface StockValueRange {
  min: number;
  max: number;
  label: string;
}
