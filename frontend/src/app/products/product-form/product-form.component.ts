import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../models/bill.model';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html'
})
export class ProductFormComponent implements OnInit {
  product: Product = {
    name: '',
    description: '',
    unit: '',
    price: 0,
    gstRate: 18,
    hsnCode: '',
    stockQuantity: 0
  };
  isEditMode: boolean = false;
  loading: boolean = false;

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadProduct(+id);
    }
  }

  loadProduct(id: number): void {
    this.productService.getProductById(id).subscribe(data => {
      this.product = data;
    });
  }

  saveProduct(): void {
    this.loading = true;
    if (this.isEditMode) {
      this.productService.updateProduct(this.product.id!, this.product).subscribe(() => {
        this.router.navigate(['/products']);
        this.loading = false;
      });
    } else {
      this.productService.createProduct(this.product).subscribe(() => {
        this.router.navigate(['/products']);
        this.loading = false;
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/products']);
  }
}
