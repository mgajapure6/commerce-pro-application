// src/app/features/inventory/stock-adjustment/stock-adjustment.component.ts
// Stock Adjustment Component - UI Guidelines Compliant

import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { 
  StockAdjustmentService,
  AdjustmentBatch,
  AdjustmentItem,
  AdjustmentType,
  AdjustmentStatus,
  AdjustmentReason 
} from '../../../core/services/inventory/stock-adjustment.service';
import { WarehouseService } from '../../../core/services/inventory/warehouse.service';
import { InventoryService } from '../../../core/services/inventory/inventory.service';
import { InventoryItem } from '../../../core/models/inventory';

@Component({
  selector: 'app-stock-adjustment',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './stock-adjustment.html',
  styleUrl: './stock-adjustment.scss'
})
export class StockAdjustmentComponent implements OnInit {
  private fb = inject(FormBuilder);
  private adjustmentService = inject(StockAdjustmentService);
  private warehouseService = inject(WarehouseService);
  private inventoryService = inject(InventoryService);

  // View State
  activeView = signal<'list' | 'create' | 'detail'>('list');
  selectedBatch = signal<AdjustmentBatch | null>(null);
  
  // Modal States
  showItemModal = signal(false);
  showDeleteModal = signal(false);
  showSubmitModal = signal(false);
  showApproveModal = signal(false);
  showRejectModal = signal(false);
  
  // Filters
  searchQuery = signal('');
  filterStatus = signal<AdjustmentStatus | 'all'>('all');
  filterType = signal<AdjustmentType | 'all'>('all');
  filterWarehouse = signal<string>('all');
  
  // Forms
  batchForm!: FormGroup;
  itemForm!: FormGroup;
  rejectForm!: FormGroup;
  
  // Data from services
  adjustmentBatches = this.adjustmentService.allAdjustments;
  warehouses = this.warehouseService.allWarehouses;
  inventoryItems = this.inventoryService.allItems;
  isLoading = this.adjustmentService.isLoading;
  hasError = this.adjustmentService.hasError;
  stats = this.adjustmentService.stats;

  // Draft items for new adjustment
  draftItems = signal<AdjustmentItem[]>([]);

  // Adjustment types
  adjustmentTypes: { value: AdjustmentType; label: string; icon: string }[] = [
    { value: 'count', label: 'Stock Count', icon: 'clipboard-check' },
    { value: 'damage', label: 'Damage/Loss', icon: 'exclamation-triangle' },
    { value: 'receiving', label: 'Receiving', icon: 'box-seam' },
    { value: 'return', label: 'Return', icon: 'arrow-return-left' },
    { value: 'transfer', label: 'Transfer', icon: 'arrow-left-right' },
    { value: 'correction', label: 'Correction', icon: 'pencil-square' },
    { value: 'expiry', label: 'Expiry', icon: 'calendar-x' }
  ];

  // Adjustment reasons
  adjustmentReasons: { value: AdjustmentReason; label: string }[] = [
    { value: 'damage', label: 'Damaged Goods' },
    { value: 'expired', label: 'Expired' },
    { value: 'lost', label: 'Lost' },
    { value: 'found', label: 'Found' },
    { value: 'theft', label: 'Theft' },
    { value: 'correction', label: 'Correction' },
    { value: 'system_error', label: 'System Error' },
    { value: 'other', label: 'Other' }
  ];

  // Computed
  filteredBatches = computed(() => {
    let result = this.adjustmentBatches();
    
    const query = this.searchQuery().toLowerCase();
    if (query) {
      result = result.filter(b => 
        b.adjustmentNumber?.toLowerCase().includes(query) ||
        b.reference?.toLowerCase().includes(query) ||
        b.notes?.toLowerCase().includes(query)
      );
    }
    
    if (this.filterStatus() !== 'all') {
      result = result.filter(b => b.status === this.filterStatus());
    }
    if (this.filterType() !== 'all') {
      result = result.filter(b => b.type === this.filterType());
    }
    if (this.filterWarehouse() !== 'all') {
      result = result.filter(b => b.warehouseId === this.filterWarehouse());
    }
    
    return [...result].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  });

  availableInventory = computed(() => {
    const warehouseId = this.batchForm?.get('warehouseId')?.value;
    if (!warehouseId) return this.inventoryItems();
    return this.inventoryItems().filter(i => i.warehouseId === warehouseId);
  });

  draftSummary = computed(() => {
    const items = this.draftItems();
    return {
      count: items.length,
      positiveQty: items.filter(i => i.difference > 0).reduce((sum, i) => sum + i.difference, 0),
      negativeQty: items.filter(i => i.difference < 0).reduce((sum, i) => sum + Math.abs(i.difference), 0),
      totalValue: items.reduce((sum, i) => sum + i.totalValue, 0)
    };
  });

  // Display stats for cards
  displayStats = computed(() => [
    { 
      label: 'Total Adjustments', 
      value: this.stats().total, 
      icon: 'clipboard-data',
      bgColor: 'bg-blue-100',
      iconColor: 'text-blue-600'
    },
    { 
      label: 'Draft', 
      value: this.stats().draft, 
      icon: 'file-earmark',
      bgColor: 'bg-gray-100',
      iconColor: 'text-gray-600'
    },
    { 
      label: 'Pending', 
      value: this.stats().pending, 
      icon: 'clock-history',
      bgColor: 'bg-yellow-100',
      iconColor: 'text-yellow-600'
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
    this.batchForm = this.fb.group({
      type: ['correction', Validators.required],
      reference: [''],
      warehouseId: ['', Validators.required],
      notes: ['', Validators.required]
    });

    this.itemForm = this.fb.group({
      inventoryItemId: ['', Validators.required],
      countedQty: [0, [Validators.required, Validators.min(0)]],
      reason: ['correction', Validators.required],
      notes: ['']
    });

    this.rejectForm = this.fb.group({
      reason: ['', Validators.required]
    });
  }

  // ==================== Navigation ====================

  goToList() {
    this.activeView.set('list');
    this.selectedBatch.set(null);
    this.draftItems.set([]);
    this.batchForm.reset();
  }

  startNewAdjustment() {
    this.draftItems.set([]);
    this.batchForm.reset({
      type: 'correction',
      reference: '',
      warehouseId: '',
      notes: ''
    });
    this.activeView.set('create');
  }

  viewBatchDetail(batch: AdjustmentBatch) {
    this.selectedBatch.set(batch);
    this.activeView.set('detail');
  }

  // ==================== Item Management ====================

  openAddItem() {
    this.itemForm.reset({
      inventoryItemId: '',
      countedQty: 0,
      reason: 'correction',
      notes: ''
    });
    this.showItemModal.set(true);
  }

  editItem(item: AdjustmentItem) {
    this.itemForm.patchValue({
      inventoryItemId: item.inventoryItemId,
      countedQty: item.countedQty,
      reason: item.reason,
      notes: item.notes
    });
    this.showItemModal.set(true);
  }

  saveItem() {
    if (this.itemForm.invalid) return;

    const formValue = this.itemForm.value;
    const inventoryItem = this.inventoryItems().find(i => i.id === formValue.inventoryItemId);
    
    if (!inventoryItem || !inventoryItem.product) return;

    const difference = formValue.countedQty - (inventoryItem.quantity || 0);
    
    const item: AdjustmentItem = {
      id: `item-${Date.now()}`,
      inventoryItemId: inventoryItem.id,
      productId: inventoryItem.productId,
      productName: inventoryItem.product.name,
      sku: inventoryItem.product.sku,
      image: inventoryItem.product.image,
      systemQty: inventoryItem.quantity || 0,
      countedQty: formValue.countedQty,
      difference: difference,
      unitCost: inventoryItem.unitCost || 0,
      totalValue: Math.abs(difference) * (inventoryItem.unitCost || 0),
      reason: formValue.reason,
      notes: formValue.notes
    };

    this.draftItems.update(items => {
      const existingIndex = items.findIndex(i => i.id === item.id);
      if (existingIndex >= 0) {
        const newItems = [...items];
        newItems[existingIndex] = item;
        return newItems;
      }
      return [...items, item];
    });

    this.showItemModal.set(false);
  }

  removeItem(itemId: string) {
    this.draftItems.update(items => items.filter(i => i.id !== itemId));
  }

  // ==================== Batch Operations ====================

  saveDraft() {
    if (this.batchForm.invalid) return;

    const formValue = this.batchForm.value;
    const newBatch: Partial<AdjustmentBatch> = {
      type: formValue.type,
      status: 'draft',
      reference: formValue.reference || undefined,
      warehouseId: formValue.warehouseId,
      notes: formValue.notes,
      items: this.draftItems(),
      totalValue: this.draftSummary().totalValue
    };

    this.adjustmentService.createAdjustment(newBatch).subscribe({
      next: () => this.goToList(),
      error: (err) => console.error('Failed to save draft:', err)
    });
  }

  submitAdjustment() {
    if (this.batchForm.invalid || this.draftItems().length === 0) return;
    this.showSubmitModal.set(true);
  }

  confirmSubmit() {
    const formValue = this.batchForm.value;
    const newBatch: Partial<AdjustmentBatch> = {
      type: formValue.type,
      status: 'pending',
      reference: formValue.reference || undefined,
      warehouseId: formValue.warehouseId,
      notes: formValue.notes,
      items: this.draftItems(),
      totalValue: this.draftSummary().totalValue
    };

    this.adjustmentService.createAdjustment(newBatch).subscribe({
      next: () => {
        this.showSubmitModal.set(false);
        this.goToList();
      },
      error: (err) => console.error('Failed to submit:', err)
    });
  }

  approveBatch() {
    this.showApproveModal.set(true);
  }

  confirmApprove() {
    const batch = this.selectedBatch();
    if (!batch) return;

    this.adjustmentService.approveAdjustment(batch.id, 'Current User').subscribe({
      next: () => {
        this.showApproveModal.set(false);
        this.selectedBatch.update(b => b ? { ...b, status: 'approved' } as AdjustmentBatch : null);
      },
      error: (err) => console.error('Failed to approve:', err)
    });
  }

  rejectBatch() {
    this.showRejectModal.set(true);
  }

  confirmReject() {
    if (this.rejectForm.invalid) return;
    
    const batch = this.selectedBatch();
    if (!batch) return;

    const reason = this.rejectForm.value.reason;

    this.adjustmentService.rejectAdjustment(batch.id, 'Current User', reason).subscribe({
      next: () => {
        this.showRejectModal.set(false);
        this.rejectForm.reset();
        this.selectedBatch.update(b => b ? { ...b, status: 'rejected' } as AdjustmentBatch : null);
      },
      error: (err) => console.error('Failed to reject:', err)
    });
  }

  completeBatch() {
    const batch = this.selectedBatch();
    if (!batch) return;

    this.adjustmentService.completeAdjustment(batch.id).subscribe({
      next: () => {
        this.selectedBatch.update(b => b ? { ...b, status: 'completed' } as AdjustmentBatch : null);
      },
      error: (err) => console.error('Failed to complete:', err)
    });
  }

  deleteBatch() {
    this.showDeleteModal.set(true);
  }

  confirmDelete() {
    const batch = this.selectedBatch();
    if (!batch) return;

    this.adjustmentService.deleteAdjustment(batch.id).subscribe({
      next: () => {
        this.showDeleteModal.set(false);
        this.goToList();
      },
      error: (err) => console.error('Failed to delete:', err)
    });
  }

  // ==================== Helper Methods ====================

  getTypeLabel(type: AdjustmentType): string {
    const labels: Record<AdjustmentType, string> = {
      'count': 'Stock Count',
      'damage': 'Damage/Loss',
      'receiving': 'Receiving',
      'return': 'Return',
      'transfer': 'Transfer',
      'correction': 'Correction',
      'expiry': 'Expiry'
    };
    return labels[type];
  }

  getTypeColor(type: AdjustmentType): string {
    const colors: Record<AdjustmentType, string> = {
      'count': 'bg-purple-100 text-purple-700',
      'damage': 'bg-red-100 text-red-700',
      'receiving': 'bg-green-100 text-green-700',
      'return': 'bg-orange-100 text-orange-700',
      'transfer': 'bg-blue-100 text-blue-700',
      'correction': 'bg-yellow-100 text-yellow-700',
      'expiry': 'bg-pink-100 text-pink-700'
    };
    return colors[type];
  }

  getTypeIcon(type: AdjustmentType): string {
    const icons: Record<AdjustmentType, string> = {
      'count': 'clipboard-check',
      'damage': 'exclamation-triangle',
      'receiving': 'box-seam',
      'return': 'arrow-return-left',
      'transfer': 'arrow-left-right',
      'correction': 'pencil-square',
      'expiry': 'calendar-x'
    };
    return icons[type];
  }

  getStatusColor(status: AdjustmentStatus): string {
    const colors: Record<AdjustmentStatus, string> = {
      'draft': 'bg-gray-100 text-gray-700',
      'pending': 'bg-yellow-100 text-yellow-700',
      'approved': 'bg-blue-100 text-blue-700',
      'rejected': 'bg-red-100 text-red-700',
      'completed': 'bg-green-100 text-green-700'
    };
    return colors[status];
  }

  getReasonLabel(reason: AdjustmentReason): string {
    const labels: Record<AdjustmentReason, string> = {
      'damage': 'Damaged Goods',
      'expired': 'Expired',
      'lost': 'Lost',
      'found': 'Found',
      'theft': 'Theft',
      'correction': 'Correction',
      'system_error': 'System Error',
      'other': 'Other'
    };
    return labels[reason];
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value || 0);
  }

  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    }).format(new Date(date));
  }

  getDifferenceColor(diff: number): string {
    if (diff > 0) return 'text-green-600';
    if (diff < 0) return 'text-red-600';
    return 'text-gray-600';
  }

  getWarehouseName(warehouseId: string): string {
    return this.warehouses().find(w => w.id === warehouseId)?.name || 'Unknown';
  }

  canEdit(status: AdjustmentStatus): boolean {
    return ['draft', 'pending'].includes(status);
  }

  canApprove(status: AdjustmentStatus): boolean {
    return status === 'pending';
  }

  canComplete(status: AdjustmentStatus): boolean {
    return status === 'approved';
  }

  retry() {
    this.adjustmentService.retry();
  }

  protected readonly Math = Math;
}
