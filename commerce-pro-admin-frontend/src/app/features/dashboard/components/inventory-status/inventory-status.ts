import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../../../core/services/catalog/product.service';
import { Product } from '../../../../core/models/catalog/product.model';

@Component({
  selector: 'app-inventory-status',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inventory-status.html',
  styleUrl: './inventory-status.scss'
})
export class InventoryStatus {
  private productService = inject(ProductService);
  
  products = this.productService.allProducts;
  isLoading = this.productService.isLoading;
  
  // Computed inventory stats
  stats = computed(() => {
    const all = this.products();
    const total = all.length;
    
    if (total === 0) {
      return {
        inStock: { count: 0, percentage: 0 },
        lowStock: { count: 0, percentage: 0 },
        outOfStock: { count: 0, percentage: 0 }
      };
    }
    
    const inStockCount = all.filter(p => p.stockStatus === 'IN_STOCK').length;
    const lowStockCount = all.filter(p => p.stockStatus === 'LOW_STOCK').length;
    const outOfStockCount = all.filter(p => p.stockStatus === 'OUT_OF_STOCK').length;
    
    return {
      inStock: { 
        count: inStockCount, 
        percentage: Math.round((inStockCount / total) * 100) 
      },
      lowStock: { 
        count: lowStockCount, 
        percentage: Math.round((lowStockCount / total) * 100) 
      },
      outOfStock: { 
        count: outOfStockCount, 
        percentage: Math.round((outOfStockCount / total) * 100) 
      }
    };
  });
  
  // Products needing attention (low or out of stock)
  attentionProducts = computed(() => {
    return this.products()
      .filter(p => p.stockStatus === 'LOW_STOCK' || p.stockStatus === 'OUT_OF_STOCK')
      .slice(0, 5);
  });
}
