import { Component, OnInit } from '@angular/core';
import { BillService } from '../services/bill.service';
import { CustomerService } from '../services/customer.service';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  totalBills: number = 0;
  totalCustomers: number = 0;
  totalProducts: number = 0;
  totalRevenue: number = 0;
  loading: boolean = true;
  currentDate: Date = new Date();

  constructor(
    private billService: BillService,
    private customerService: CustomerService,
    private productService: ProductService
  ) { }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.billService.getBills().subscribe(bills => {
      this.totalBills = bills.length;
      this.totalRevenue = bills.reduce((sum, bill) => sum + (bill.totalAmount || 0), 0);
      this.loading = false;
    });

    this.customerService.getCustomers().subscribe(customers => {
      this.totalCustomers = customers.length;
    });

    this.productService.getProducts().subscribe(products => {
      this.totalProducts = products.length;
    });
  }
}
