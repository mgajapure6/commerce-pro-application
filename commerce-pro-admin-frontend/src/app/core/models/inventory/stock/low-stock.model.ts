// Low Stock Alert model - aligned with backend LowStockAlertDTO

export interface LowStockAlert {
  id: string;
  inventoryItemId: string;
  productId: string;
  productName: string;
  productSku: string;
  productImage?: string;
  category?: string;
  
  warehouseId: string;
  warehouseName: string;
  
  currentStock: number;
  availableStock: number;
  lowStockThreshold: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  
  status: 'CRITICAL' | 'LOW' | 'REORDER';
  severity: 'critical' | 'warning' | 'info';
  daysUntilStockout?: number;
  avgDailyUsage?: number;
  
  lastRestocked?: Date;
  suggestedOrderDate?: Date;
  
  // UI state properties
  acknowledged: boolean;
  resolved: boolean;
  resolvedAt?: Date;
  isRead: boolean;
  
  createdAt: Date;
  updatedAt: Date;
}

export interface LowStockFilter {
  warehouseId?: string;
  category?: string;
  status?: 'CRITICAL' | 'LOW' | 'REORDER';
  severity?: 'critical' | 'warning' | 'info';
  searchQuery?: string;
  acknowledged?: boolean;
}

export interface ReorderSuggestion {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  currentStock: number;
  suggestedQuantity: number;
  unitCost?: number;
  totalCost?: number;
  supplier?: string;
  supplierName?: string;
  leadTimeDays?: number;
  estimatedDeliveryDate?: Date;
}

export interface AlertConfig {
  enabled: boolean;
  lowStockThreshold: number;
  criticalStockThreshold: number;
  emailNotification: boolean;
  smsNotification: boolean;
  notifyRoles: string[];
}

// Stats type for low stock dashboard
export interface LowStockStats {
  total: number;
  critical: number;
  low: number;
  reorder: number;
  acknowledged: number;
  unacknowledged: number;
  unreadAlerts: number;
  
  // Additional stats for dashboard
  totalItems: number;
  criticalCount: number;
  lowCount: number;
  adequateCount: number;
  excessCount: number;
  poSuggestions: number;
  totalShortageValue: number;
  avgDaysUntilStockout: number;
}
