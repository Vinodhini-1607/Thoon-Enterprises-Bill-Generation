import { Component, OnInit } from '@angular/core';
import { Bill } from '../../models/bill.model';
import { BillService } from '../../services/bill.service';

@Component({
  selector: 'app-bill-list',
  templateUrl: './bill-list.component.html'
})
export class BillListComponent implements OnInit {
  bills: Bill[] = [];
  loading: boolean = true;

  constructor(private billService: BillService) { }

  ngOnInit(): void {
    this.loadBills();
  }

  loadBills(): void {
    this.billService.getBills().subscribe(data => {
      this.bills = data.sort((a, b) => {
        const dateA = new Date(a.createdAt || 0);
        const dateB = new Date(b.createdAt || 0);
        return dateB.getTime() - dateA.getTime();
      });
      this.loading = false;
    });
  }

  deleteBill(id: number): void {
    if (confirm('Are you sure you want to delete this bill?')) {
      this.billService.deleteBill(id).subscribe(() => {
        this.loadBills();
      });
    }
  }

  downloadPdf(id: number, billNumber: string): void {
    this.billService.downloadBillPdf(id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `bill_${billNumber}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading PDF:', error);
        alert('Error downloading PDF. Please try again.');
      }
    });
  }
}
