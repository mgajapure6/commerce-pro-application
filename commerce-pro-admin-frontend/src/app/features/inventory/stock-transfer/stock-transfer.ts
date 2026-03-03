// src/app/features/inventory/stock-transfer/stock-transfer.component.ts
// Stock Transfer Component - UI Guidelines Compliant

import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { 
  StockTransferService,
  StockTransfer,
  TransferItem,
  TransferStatus,
  TransferPriority,
  TransferType
} from '../../../core/services/inventory/stock-transfer.service';
import { WarehouseService } from '../../../core/services/inventory/warehouse.service';
import { InventoryService } from '../../../core/services/inventory/inventory.service';
import { InventoryItem } from '../../../core/models/inventory';

@Component({
  selector: 'app-stock-transfer',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './stock-transfer.html',
  styleUrl: './stock-transfer.scss'
})
export class StockTransferComponent implements OnInit {
  private fb = inject(FormBuilder);
  private transferService = inject(StockTransferService);
  private warehouseService = inject(WarehouseService);
  private inventoryService = inject(InventoryService);

  // View State
  activeView = signal<'list' | 'create' | 'detail'>('list');
  selectedTransfer = signal<StockTransfer | null>(null);
  
  // Modal States
  showItemModal = signal(false);
  showDeleteModal = signal(false);
  showSubmitModal = signal(false);
  showApproveModal = signal(false);
  showShipModal = signal(false);
  showReceiveModal = signal(false);
  showCancelModal = signal(false);
  
  // Filters
  searchQuery = signal('');
  filterStatus = signal<TransferStatus | 'all'>('all');
  filterPriority = signal<TransferPriority | 'all'>('all');
  filterType = signal<TransferType | 'all'>('all');
  filterFromWarehouse = signal<string>('all');
  filterToWarehouse = signal<string>('all');
  
  // Forms
  transferForm!: FormGroup;
  itemForm!: FormGroup;
  
  // Data from services
  transfers = this.transferService.allTransfers;
  warehouses = this.warehouseService.allWarehouses;
  inventoryItems = this.inventoryService.allItems;
  isLoading = this.transferService.isLoading;
  hasError = this.transferService.hasError;
  stats = this.transferService.stats;

  // Draft items for new transfer
  draftItems = signal<TransferItem[]>([]);

  // Transfer types
  transferTypes: { value: TransferType; label: string }[] = [
    { value: 'standard', label: 'Standard Transfer' },
    { value: 'replenishment', label: 'Replenishment' },
    { value: 'returns', label: 'Returns' },
    { value: 'consignment', label: 'Consignment' }
  ];

  // Transfer priorities
  transferPriorities: { value: TransferPriority; label: string; color: string }[] = [
    { value: 'low', label: 'Low', color: 'bg-gray-100 text-gray-700' },
    { value: 'normal', label: 'Normal', color: 'bg-blue-100 text-blue-700' },
    { value: 'high', label: 'High', color: 'bg-orange-100 text-orange-700' },
    { value: 'urgent', label: 'Urgent', color: 'bg-red-100 text-red-700' }
  ];

  // Computed
  filteredTransfers = computed(() => {
    let result = this.transfers();
    
    const query = this.searchQuery().toLowerCase();
    if (query) {
      result = result.filter(t => 
        t.transferNumber?.toLowerCase().includes(query) ||
        t.reference?.toLowerCase().includes(query) ||
        t.notes?.toLowerCase().includes(query)
      );
    }
    
    if (this.filterStatus() !== 'all') {
      result = result.filter(t => t.status === this.filterStatus());
    }
    if (this.filterPriority() !== 'all') {
      result = result.filter(t => t.priority === this.filterPriority());
    }
    if (this.filterType() !== 'all') {
      result = result.filter(t => t.type === this.filterType());
    }
    if (this.filterFromWarehouse() !== 'all') {
      result = result.filter(t => t.fromWarehouseId === this.filterFromWarehouse());
    }
    if (this.filterToWarehouse() !== 'all') {
      result = result.filter(t => t.toWarehouseId === this.filterToWarehouse());
    }
    
    return [...result].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  });

  availableProducts = computed(() => {
    const fromWarehouseId = this.transferForm?.get('fromWarehouseId')?.value;
    if (!fromWarehouseId) return [];
    
    return this.inventoryItems().filter(i => i.warehouseId === fromWarehouseId);
  });

  draftSummary = computed(() => {
    const items = this.draftItems();
    return {
      count: items.length,
      totalQty: items.reduce((sum, i) => sum + i.quantity, 0),
      totalValue: items.reduce((sum, i) => sum + i.totalValue, 0)
    };
  });

  // Display stats for cards
  displayStats = computed(() => [
    { 
      label: 'Total Transfers', 
      value: this.stats().total, 
      icon: 'truck',
      bgColor: 'bg-blue-100',
      iconColor: 'text-blue-600'
    },
    { 
      label: 'Pending', 
      value: this.stats().pending, 
      icon: 'clock',
      bgColor: 'bg-yellow-100',
      iconColor: 'text-yellow-600'
    },
    { 
      label: 'In Transit', 
      value: this.stats().inTransit, 
      icon: 'box-seam',
      bgColor: 'bg-purple-100',
      iconColor: 'text-purple-600'
    },
    { 
      label: 'Completed', 
      value: this.stats().completed, 
      icon: 'check-circle',
      bgColor: 'bg-green-100',
      iconColor: 'text-green-600'
    }
  ]);

  ngOnInit() {
    this.initializeForms();
  }

  initializeForms() {
    this.transferForm = this.fb.group({
      type: ['standard', Validators.required],
      priority: ['normal', Validators.required],
      fromWarehouseId: ['', Validators.required],
      toWarehouseId: ['', Validators.required],
      reference: [''],
      notes: ['']
    });

    this.itemForm = this.fb.group({
      inventoryItemId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      notes: ['']
    });
  }

  // ==================== Navigation ====================

  goToList() {
    this.activeView.set('list');
    this.selectedTransfer.set(null);
    this.draftItems.set([]);
    this.transferForm.reset();
  }

  startNewTransfer() {
    this.draftItems.set([]);
    this.transferForm.reset({
      type: 'standard',
      priority: 'normal',
      fromWarehouseId: '',
      toWarehouseId: '',
      reference: '',
      notes: ''
    });
    this.activeView.set('create');
  }

  viewTransferDetail(transfer: StockTransfer) {
    this.selectedTransfer.set(transfer);
    this.activeView.set('detail');
  }

  // ==================== Item Management ====================

  openAddItem() {
    this.itemForm.reset({
      inventoryItemId: '',
      quantity: 1,
      notes: ''
    });
    this.showItemModal.set(true);
  }

  saveItem() {
    if (this.itemForm.invalid) return;

    const formValue = this.itemForm.value;
    const inventoryItem = this.inventoryItems().find(i => i.id === formValue.inventoryItemId);
    
    if (!inventoryItem || !inventoryItem.product) return;

    const existingIndex = this.draftItems().findIndex(i => i.productId === inventoryItem.productId);
    
    if (existingIndex >= 0) {
      this.draftItems.update(items => {
        const newItems = [...items];
        newItems[existingIndex].quantity += formValue.quantity;
        newItems[existingIndex].totalValue = newItems[existingIndex].quantity * (inventoryItem.unitCost || 0);
        return newItems;
      });
    } else {
      const item: TransferItem = {
        id: `item-${Date.now()}`,
        productId: inventoryItem.productId,
        productName: inventoryItem.product.name,
        sku: inventoryItem.product.sku,
        image: inventoryItem.product.image,
        quantity: formValue.quantity,
        unitCost: inventoryItem.unitCost || 0,
        totalValue: formValue.quantity * (inventoryItem.unitCost || 0),
        notes: formValue.notes
      };
      this.draftItems.update(items => [...items, item]);
    }

    this.showItemModal.set(false);
  }

  removeItem(itemId: string) {
    this.draftItems.update(items => items.filter(i => i.id !== itemId));
  }

  // ==================== Transfer Operations ====================

  saveDraft() {
    if (this.transferForm.invalid) return;

    const formValue = this.transferForm.value;
    const newTransfer: Partial<StockTransfer> = {
      type: formValue.type,
      priority: formValue.priority,
      fromWarehouseId: formValue.fromWarehouseId,
      toWarehouseId: formValue.toWarehouseId,
      status: 'draft',
      reference: formValue.reference || undefined,
      notes: formValue.notes,
      items: this.draftItems(),
      totalValue: this.draftSummary().totalValue
    };

    this.transferService.createTransfer(newTransfer).subscribe({
      next: () => this.goToList(),
      error: (err) => console.error('Failed to save draft:', err)
    });
  }

  submitTransfer() {
    if (this.transferForm.invalid || this.draftItems().length === 0) return;
    this.showSubmitModal.set(true);
  }

  confirmSubmit() {
    const formValue = this.transferForm.value;
    const newTransfer: Partial<StockTransfer> = {
      type: formValue.type,
      priority: formValue.priority,
      fromWarehouseId: formValue.fromWarehouseId,
      toWarehouseId: formValue.toWarehouseId,
      status: 'pending',
      reference: formValue.reference || undefined,
      notes: formValue.notes,
      items: this.draftItems(),
      totalValue: this.draftSummary().totalValue
    };

    this.transferService.createTransfer(newTransfer).subscribe({
      next: () => {
        this.showSubmitModal.set(false);
        this.goToList();
      },
      error: (err) => console.error('Failed to submit:', err)
    });
  }

  approveTransfer() {
    this.showApproveModal.set(true);
  }

  confirmApprove() {
    const transfer = this.selectedTransfer();
    if (!transfer) return;

    this.transferService.approveTransfer(transfer.id, 'Current User').subscribe({
      next: () => {
        this.showApproveModal.set(false);
        this.selectedTransfer.update(t => t ? { ...t, status: 'approved' } as StockTransfer : null);
      },
      error: (err) => console.error('Failed to approve:', err)
    });
  }

  shipTransfer() {
    this.showShipModal.set(true);
  }

  confirmShip() {
    const transfer = this.selectedTransfer();
    if (!transfer) return;

    this.transferService.shipTransfer(transfer.id).subscribe({
      next: () => {
        this.showShipModal.set(false);
        this.selectedTransfer.update(t => t ? { ...t, status: 'shipped' } as StockTransfer : null);
      },
      error: (err) => console.error('Failed to ship:', err)
    });
  }

  receiveTransfer() {
    this.showReceiveModal.set(true);
  }

  confirmReceive() {
    const transfer = this.selectedTransfer();
    if (!transfer) return;

    this.transferService.receiveTransfer(transfer.id).subscribe({
      next: () => {
        this.showReceiveModal.set(false);
        this.selectedTransfer.update(t => t ? { ...t, status: 'received' } as StockTransfer : null);
      },
      error: (err) => console.error('Failed to receive:', err)
    });
  }

  completeTransfer() {
    const transfer = this.selectedTransfer();
    if (!transfer) return;

    this.transferService.completeTransfer(transfer.id).subscribe({
      next: () => {
        this.selectedTransfer.update(t => t ? { ...t, status: 'completed' } as StockTransfer : null);
      },
      error: (err) => console.error('Failed to complete:', err)
    });
  }

  cancelTransfer() {
    this.showCancelModal.set(true);
  }

  confirmCancel() {
    const transfer = this.selectedTransfer();
    if (!transfer) return;

    this.transferService.cancelTransfer(transfer.id).subscribe({
      next: () => {
        this.showCancelModal.set(false);
        this.selectedTransfer.update(t => t ? { ...t, status: 'cancelled' } as StockTransfer : null);
      },
      error: (err) => console.error('Failed to cancel:', err)
    });
  }

  deleteTransfer() {
    this.showDeleteModal.set(true);
  }

  confirmDelete() {
    const transfer = this.selectedTransfer();
    if (!transfer) return;

    this.transferService.deleteTransfer(transfer.id).subscribe({
      next: () => {
        this.showDeleteModal.set(false);
        this.goToList();
      },
      error: (err) => console.error('Failed to delete:', err)
    });
  }

  // ==================== Helper Methods ====================

  getTypeLabel(type: TransferType): string {
    const labels: Record<TransferType, string> = {
      'standard': 'Standard',
      'replenishment': 'Replenishment',
      'returns': 'Returns',
      'consignment': 'Consignment'
    };
    return labels[type];
  }

  getStatusColor(status: TransferStatus): string {
    const colors: Record<TransferStatus, string> = {
      'draft': 'bg-gray-100 text-gray-700',
      'pending': 'bg-yellow-100 text-yellow-700',
      'approved': 'bg-blue-100 text-blue-700',
      'shipped': 'bg-purple-100 text-purple-700',
      'in_transit': 'bg-orange-100 text-orange-700',
      'received': 'bg-cyan-100 text-cyan-700',
      'completed': 'bg-green-100 text-green-700',
      'cancelled': 'bg-red-100 text-red-700'
    };
    return colors[status];
  }

  getPriorityLabel(priority: TransferPriority): string {
    const labels: Record<TransferPriority, string> = {
      'low': 'Low',
      'normal': 'Normal',
      'high': 'High',
      'urgent': 'Urgent'
    };
    return labels[priority];
  }

  getPriorityColor(priority: TransferPriority): string {
    const colors: Record<TransferPriority, string> = {
      'low': 'bg-gray-100 text-gray-700',
      'normal': 'bg-blue-100 text-blue-700',
      'high': 'bg-orange-100 text-orange-700',
      'urgent': 'bg-red-100 text-red-700'
    };
    return colors[priority];
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value || 0);
  }

  formatDate(date?: Date): string {
    if (!date) return '-';
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    }).format(new Date(date));
  }

  getWarehouseName(warehouseId: string): string {
    return this.warehouses().find(w => w.id === warehouseId)?.name || 'Unknown';
  }

  canEdit(status: TransferStatus): boolean {
    return ['draft', 'pending'].includes(status);
  }

  canApprove(status: TransferStatus): boolean {
    return status === 'pending';
  }

  canShip(status: TransferStatus): boolean {
    return ['approved', 'pending'].includes(status);
  }

  canReceive(status: TransferStatus): boolean {
    return ['shipped', 'in_transit'].includes(status);
  }

  canCancel(status: TransferStatus): boolean {
    return ['draft', 'pending', 'approved'].includes(status);
  }

  retry() {
    this.transferService.retry();
  }

  protected readonly Math = Math;
}
