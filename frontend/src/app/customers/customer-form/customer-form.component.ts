import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Customer } from '../../models/bill.model';
import { CustomerService } from '../../services/customer.service';

@Component({
  selector: 'app-customer-form',
  templateUrl: './customer-form.component.html',
  styleUrls: ['./customer-form.component.css']
})
export class CustomerFormComponent implements OnInit {
  customer: Customer = {
    name: '',
    address: '',
    phone: '',
    email: '',
    gstNumber: ''
  };
  isEditMode: boolean = false;
  loading: boolean = false;

  constructor(
    private customerService: CustomerService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadCustomer(+id);
    }
  }

  loadCustomer(id: number): void {
    this.customerService.getCustomerById(id).subscribe(data => {
      this.customer = data;
    });
  }

  saveCustomer(): void {
    this.loading = true;
    if (this.isEditMode) {
      this.customerService.updateCustomer(this.customer.id!, this.customer).subscribe(() => {
        this.router.navigate(['/customers']);
        this.loading = false;
      });
    } else {
      this.customerService.createCustomer(this.customer).subscribe(() => {
        this.router.navigate(['/customers']);
        this.loading = false;
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/customers']);
  }
}
