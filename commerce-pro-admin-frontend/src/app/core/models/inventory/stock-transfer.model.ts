// src/app/core/models/inventory/stock-transfer.model.ts
// Stock transfer between warehouses/models

export type TransferStatus = 'draft' | 'pending' | 'approved' | 'shipped' | 'in_transit' | 'received' | 'completed' | 'cancelled';

export type TransferPriority = 'low' | 'normal' | 'high' | 'urgent';

export type TransferType = 'standard' | 'replenishment' | 'returns' | 'consignment';

export interface TransferLineItem {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  image?: string;
  quantityRequested: number;
  quantityShipped: number;
  quantityReceived: number;
  unitCost: number;
  totalValue: number;
  notes?: string;
  sourceBin?: string;
}

export type ShipmentDocumentType = 'bill_of_lading' | 'packing_list' | 'invoice' | 'customs_doc' | 'certificate';

export interface ShipmentDocument {
  id: string;
  type: ShipmentDocumentType;
  name: string;
  url: string;
  uploadedAt: Date;
}

export type ShipmentStatus = 'pending' | 'in_transit' | 'delivered' | 'exception';

export interface ShipmentInfo {
  carrier: string;
  trackingNumber: string;
  trackingUrl?: string;
  estimatedDelivery: Date;
  actualDelivery?: Date;
  status: ShipmentStatus;
  documents: ShipmentDocument[];
  notes?: string;
}

export interface StockTransfer {
  id: string;
  transferNumber: string;
  type: TransferType;
  status: TransferStatus;
  priority: TransferPriority;
  fromWarehouseId: string;
  fromWarehouseName: string;
  toWarehouseId: string;
  toWarehouseName: string;
  items: TransferLineItem[];
  totalItems: number;
  totalValue: number;
  requestedBy: string;
  approvedBy?: string;
  approvedAt?: Date;
  shippedAt?: Date;
  receivedAt?: Date;
  completedAt?: Date;
  shipmentInfo?: ShipmentInfo;
  notes?: string;
  reason?: string;
  createdAt: Date;
  updatedAt: Date;
}
