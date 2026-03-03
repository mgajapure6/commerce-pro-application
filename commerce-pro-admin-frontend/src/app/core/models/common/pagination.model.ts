// src/app/core/models/common/pagination.model.ts
// Pagination and sorting models

/**
 * Pageable parameters for API requests
 * Matches Spring Boot Pageable
 */
export interface PageParams {
  page?: number;        // 0-based page index
  size?: number;        // Items per page
  sort?: string;        // Sort field
  direction?: 'asc' | 'desc'; // Sort direction
}

/**
 * Sort configuration
 */
export interface SortConfig {
  field: string;
  direction: 'asc' | 'desc';
}

/**
 * Pagination state for UI components
 */
export interface PaginationState {
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * Default pagination values
 */
export const DEFAULT_PAGINATION: PageParams = {
  page: 0,
  size: 20,
  sort: 'createdAt',
  direction: 'desc'
};

/**
 * Build HttpParams from PageParams
 */
export function buildPageParams(params: PageParams): { [key: string]: string } {
  const result: { [key: string]: string } = {};
  
  if (params.page !== undefined) {
    result['page'] = params.page.toString();
  }
  if (params.size !== undefined) {
    result['size'] = params.size.toString();
  }
  if (params.sort) {
    const direction = params.direction || 'asc';
    result['sort'] = `${params.sort},${direction}`;
  }
  
  return result;
}
