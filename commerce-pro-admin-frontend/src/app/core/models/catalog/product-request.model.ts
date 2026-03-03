// src/app/core/models/catalog/product-request.model.ts
// Product request DTOs for create/update operations

import { ProductDimensions, ProductVariant } from './product.model';

/**
 * Product Create/Update Request DTO
 * Matches backend ProductRequestDTO
 */
export interface ProductRequest {
  // Required fields
  name: string;
  sku: string;
  category: string;
  brand: string;
  price: number;
  stock: number;
  lowStockThreshold: number;
  status: 'active' | 'draft' | 'archived' | 'out_of_stock' | 'discontinued';
  visibility: 'visible' | 'hidden';
  featured: boolean;
  trackInventory: boolean;
  allowBackorders: boolean;
  
  // Optional fields
  description?: string;
  shortDescription?: string;
  categoryId?: string;
  compareAtPrice?: number;
  cost?: number;
  image?: string;
  gallery?: string[];
  featuredImage?: string;
  weight?: number;
  dimensions?: ProductDimensions;
  tags?: string[];
  variants?: ProductVariant[];
  vendor?: string;
  productType?: 'Physical' | 'Digital';
  barcode?: string;
  urlHandle?: string;
  seoTitle?: string;
  seoDescription?: string;
  imageAlt?: string;
}

/**
 * Stock Update Request DTO
 * Matches backend StockUpdateDTO
 */
export interface StockUpdateRequest {
  quantity: number;
  reason: string;
  adjust?: boolean; // If true, adds to existing stock; if false, sets absolute value
  notes?: string;
}

/**
 * Bulk Status Update Request
 */
export interface BulkStatusUpdateRequest {
  ids: string[];
  status: string;
}

/**
 * Bulk Delete Request
 */
export interface BulkDeleteRequest {
  ids: string[];
}
