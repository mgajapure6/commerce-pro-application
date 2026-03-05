// src/app/core/models/index.ts
// Central export for all models

// Product models
export * from './catalog/product.model';

// Category models
export * from './catalog/category.model';

// Attribute models
export * from './attribute.model';

// Brand models
export * from './brand.model';

// Collection models
export * from './collection.model';

// Review models
export * from './review.model';

// SEO models
export * from './seo.model';

// Catalog shared models (bulk operations, etc.)
export * from './catalog-shared.model';

// Common/Shared API models (ApiResponse, Pagination, etc.)
export * from './common';

// Backend API DTOs - Catalog specific (Product requests/responses)
export * from './catalog/product-request.model';
export * from './catalog/product-response.model';

// Dashboard models
export * from './dashboard.model';

// Inventory models
export * from './inventory';

// Identity models
export * from './identity';

// Auth models
export * from './auth';

// Legacy exports for backward compatibility
export type { Product as ProductModel } from './catalog/product.model';
export type { Category as CategoryModel } from './catalog/category.model';
