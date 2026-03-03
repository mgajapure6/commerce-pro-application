// src/app/core/models/common/api-response.model.ts
// API Response wrappers matching Spring Boot backend

/**
 * Standard API response wrapper from backend
 */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  error?: any;
  timestamp: number;
  path?: string;
}

/**
 * Paginated response matching Spring Boot PageResponse
 */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/**
 * Generic API Error response
 */
export interface ApiError {
  code: string;
  message: string;
  details?: any;
}
