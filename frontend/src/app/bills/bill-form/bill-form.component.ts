import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BillRequest, BillItem, Customer, Product } from '../../models/bill.model';
import { BillService } from '../../services/bill.service';
import { CustomerService } from '../../services/customer.service';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-bill-form',
  templateUrl: './bill-form.component.html'
})
export class BillFormComponent implements OnInit {
  customers: Customer[] = [];
  products: Product[] = [];
  
  billRequest: BillRequest = {
    customerId: 0,
    billDate: new Date().toISOString().split('T')[0],
    isInterState: false,
    items: []
  };
  
  newItem: BillItem = {
    productId: 0,
    quantity: 1,
    unitPrice: 0,
    gstRate: 18
  };

  get newItemTotal(): number {
    return this.newItem.quantity * this.newItem.unitPrice;
  }

  get newItemTotalWithGST(): number {
    const lineTotal = this.newItem.quantity * this.newItem.unitPrice;
    const gst = lineTotal * (this.newItem.gstRate / 100);
    return lineTotal + gst;
  }
  
  loading: boolean = false;

  constructor(
    private billService: BillService,
    private customerService: CustomerService,
    private productService: ProductService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCustomers();
    this.loadProducts();
  }

  loadCustomers(): void {
    this.customerService.getCustomers().subscribe(data => {
      this.customers = data;
    });
  }

  loadProducts(): void {
    this.productService.getProducts().subscribe(data => {
      this.products = data;
    });
  }

  onProductChange(): void {
    const product = this.products.find(p => p.id === this.newItem.productId);
    if (product) {
      this.newItem.unitPrice = product.price;
      this.newItem.gstRate = product.gstRate;
    } else {
      this.newItem.unitPrice = 0;
      this.newItem.gstRate = 18;
    }
  }



  addItem(): void {
    if (!this.newItem.productId) {
      alert('Please select a product');
      return;
    }
    
    if (!this.newItem.quantity || this.newItem.quantity <= 0) {
      alert('Please enter a valid quantity');
      return;
    }
    
    const product = this.products.find(p => p.id === this.newItem.productId);
    if (!product) {
      alert('Product not found');
      return;
    }
    
    this.newItem.unitPrice = product.price;
    this.newItem.gstRate = product.gstRate;
    this.billRequest.items.push({ ...this.newItem });
    
    // Reset form for next item
    this.newItem = {
      productId: 0,
      quantity: 1,
      unitPrice: 0,
      gstRate: 18
    };
  }

  removeItem(index: number): void {
    this.billRequest.items.splice(index, 1);
  }

  saveBill(): void {
    if (!this.billRequest.customerId) {
      alert('Please select a customer');
      return;
    }
    
    if (this.billRequest.items.length === 0) {
      alert('Please add at least one item to the bill');
      return;
    }
    
    this.loading = true;
    console.log('Creating bill with data:', this.billRequest);
    
    this.billService.createBill(this.billRequest).subscribe({
      next: (response) => {
        console.log('Bill created successfully:', response);
        alert('Bill created successfully!');
        this.router.navigate(['/bills']);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error creating bill:', error);
        alert('Error creating bill: ' + (error.error?.message || error.message || 'Unknown error'));
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/bills']);
  }

  calculateTotal(): number {
    return this.billRequest.items.reduce((sum, item) => {
      const lineTotal = item.quantity * item.unitPrice;
      const gst = lineTotal * (item.gstRate / 100);
      return sum + lineTotal + gst;
    }, 0);
  }

  getProductName(productId: number): string {
    const product = this.products.find(p => p.id === productId);
    return product ? product.name : 'Unknown';
  }

  calculateItemTotal(item: BillItem): number {
    const lineTotal = item.quantity * item.unitPrice;
    const gst = lineTotal * (item.gstRate / 100);
    return lineTotal + gst;
  }
}
