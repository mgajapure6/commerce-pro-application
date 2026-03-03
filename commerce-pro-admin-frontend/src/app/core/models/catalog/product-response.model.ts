// src/app/core/models/catalog/product-response.model.ts
// Product response DTOs from backend

import { 
  ProductStatus, 
  StockStatus, 
  ProductVisibility, 
  ProductDimensions, 
  ProductVariant,
  ProductSummary,
  ProductDashboardView,
  ProductStats,
  ProductStatusCount,
  ProductFilterState
} from './product.model';

/**
 * Full Product Response DTO
 * Matches backend ProductResponseDTO
 */
export interface ProductResponse {
  id: string;
  name: string;
  sku: string;
  description?: string;
  shortDescription?: string;
  category: string;
  categoryId?: string;
  brand: string;
  price: number;
  compareAtPrice?: number;
  comparePrice?: number; // Alias for compareAtPrice
  cost?: number;
  stock: number;
  quantity?: number; // Alias for stock
  lowStockThreshold: number;
  stockStatus: StockStatus;
  status: ProductStatus;
  visibility: ProductVisibility;
  image: string;
  gallery?: string[];
  galleryImages?: string[]; // Alias for gallery
  featuredImage?: string | null;
  weight?: number;
  dimensions?: ProductDimensions;
  tags: string[];
  featured: boolean;
  rating: number;
  reviewCount: number;
  reviews?: number; // Alias for reviewCount
  salesCount: number;
  sales?: number; // Alias for salesCount
  revenue: number;
  variants?: ProductVariant[];
  variantCount?: number;
  hasOrders?: boolean;
  trackInventory?: boolean;
  allowBackorders?: boolean;
  vendor?: string;
  productType?: 'Physical' | 'Digital';
  barcode?: string;
  urlHandle?: string;
  seoTitle?: string;
  seoDescription?: string;
  imageAlt?: string;
  createdAt: string; // ISO date string from backend
  updatedAt: string; // ISO date string from backend
}

/**
 * Product Statistics with status breakdown
 * Extends ProductStats with additional backend data
 */
export interface ProductStatsResponse extends ProductStats {
  statusCounts: Record<string, number>;
}

// Re-export for convenience
export type { ProductSummary, ProductDashboardView, ProductFilterState, ProductStatusCount };
