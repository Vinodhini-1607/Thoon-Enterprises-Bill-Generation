import { Component, OnInit } from '@angular/core';
import { Customer } from '../../models/bill.model';
import { CustomerService } from '../../services/customer.service';

@Component({
  selector: 'app-customer-list',
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.css']
})
export class CustomerListComponent implements OnInit {
  customers: Customer[] = [];
  loading: boolean = true;
  searchKeyword: string = '';

  constructor(private customerService: CustomerService) { }

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.customerService.getCustomers().subscribe(data => {
      this.customers = data;
      this.loading = false;
    });
  }

  searchCustomers(): void {
    if (this.searchKeyword.trim()) {
      this.customerService.searchCustomers(this.searchKeyword).subscribe(data => {
        this.customers = data;
      });
    } else {
      this.loadCustomers();
    }
  }

  deleteCustomer(id: number): void {
    if (confirm('Are you sure you want to delete this customer?')) {
      this.customerService.deleteCustomer(id).subscribe(() => {
        this.loadCustomers();
      });
    }
  }
}
