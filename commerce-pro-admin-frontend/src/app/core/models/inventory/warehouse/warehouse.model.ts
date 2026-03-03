// Warehouse model - matches backend WarehouseDTO
export interface Warehouse {
  id: string;
  name: string;
  code?: string;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
  managerName?: string;
  managerEmail?: string;
  managerPhone?: string;
  isActive?: boolean;
  isDefault?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface WarehouseRequest {
  name: string;
  code?: string;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
  managerName?: string;
  managerEmail?: string;
  managerPhone?: string;
  isActive?: boolean;
  isDefault?: boolean;
}

export interface WarehouseCounts {
  total: number;
  active: number;
  inactive: number;
  default?: string;
}

export interface WarehouseStats {
  totalCapacity: number;
  usedSpace: number;
  utilizationRate: number;
  totalBins: number;
  occupiedBins: number;
  availableBins: number;
  incomingShipments: number;
  outgoingShipments: number;
  pendingTransfers: number;
  lowStockItems: number;
}