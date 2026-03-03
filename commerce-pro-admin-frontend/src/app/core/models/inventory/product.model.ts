// src/app/core/models/inventory/product.model.ts
// Inventory-specific Product and related types

export type InventoryStockStatus = 'in-stock' | 'low-stock' | 'out-of-stock' | 'overstock' | 'discontinued';

export interface Supplier {
  id: string;
  name: string;
  code: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: string;
  leadTime: number;
  rating: number;
  isActive: boolean;
  leadTimeDays?: number;
  minOrderValue?: number;
}

export interface InventoryProduct {
  id: string;
  name: string;
  sku: string;
  variant?: string;
  image: string;
  category: string;
  subcategory: string;
  brand: string;
  unitCost: number;
  unitOfMeasure: string;
  weight: number;
  dimensions: {
    length: number;
    width: number;
    height: number;
  };
  suppliers: Supplier[];
  preferredSupplier?: string;
  leadTimeDays?: number;
  isHazmat: boolean;
  isFragile: boolean;
  requiresColdStorage: boolean;
}

// Alias for backward compatibility
export type Product = InventoryProduct;

// StockStatus alias for backward compatibility  
export type StockStatus = InventoryStockStatus;
