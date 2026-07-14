import { Component, OnInit } from '@angular/core';
import { Product } from '../../models/bill.model';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html'
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  loading: boolean = true;
  searchKeyword: string = '';

  constructor(private productService: ProductService) { }

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService.getProducts().subscribe(data => {
      this.products = data;
      this.loading = false;
    });
  }

  searchProducts(): void {
    if (this.searchKeyword.trim()) {
      this.productService.searchProducts(this.searchKeyword).subscribe(data => {
        this.products = data;
      });
    } else {
      this.loadProducts();
    }
  }

  deleteProduct(id: number): void {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(id).subscribe(() => {
        this.loadProducts();
      });
    }
  }
}
