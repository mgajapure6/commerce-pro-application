// Inventory Statistics model - matches backend InventoryStatsDTO

export interface WarehouseInventoryStats {
  warehouseName: string;
  itemCount: number;
  totalUnits: number;
  totalValue: number;
}

export interface InventoryStats {
  totalItems: number;
  totalProducts: number;
  totalWarehouses: number;
  
  inStockCount: number;
  lowStockCount: number;
  outOfStockCount: number;
  overstockCount: number;
  notTrackedCount: number;
  
  totalInventoryValue: number;
  totalUnits: number;
  totalReserved: number;
  totalAvailable: number;
  totalIncoming: number;
  
  averageUnitCost: number;
  
  statusBreakdown: Record<string, number>;
  warehouseBreakdown: Record<string, WarehouseInventoryStats>;
}
