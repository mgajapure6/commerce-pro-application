// Stock Movement model - matches backend StockMovementDTO

export type MovementType = 'IN' | 'OUT' | 'ADJUSTMENT' | 'TRANSFER_IN' | 'TRANSFER_OUT' | 'RETURN' | 'DAMAGED';

export type ReferenceType = 'PURCHASE_ORDER' | 'SALES_ORDER' | 'TRANSFER' | 'ADJUSTMENT' | 'RETURN' | 'COUNT';

export interface StockMovement {
  id: string;
  inventoryId: string;
  productId: string;
  warehouseId: string;
  productName?: string;
  warehouseName?: string;
  
  type: MovementType;
  quantity: number;
  previousQuantity: number;
  newQuantity: number;
  
  reason?: string;
  notes?: string;
  reference?: string;
  referenceType?: ReferenceType;
  
  createdBy?: string;
  createdAt: Date;
}

export interface StockUpdateRequest {
  quantity: number;
  adjust?: boolean;
  reason: string;
  notes?: string;
  reference?: string;
  referenceType?: string;
}

export interface StockTransferRequest {
  itemId: string;
  fromWarehouseId: string;
  toWarehouseId: string;
  quantity: number;
  notes?: string;
  reference?: string;
}
