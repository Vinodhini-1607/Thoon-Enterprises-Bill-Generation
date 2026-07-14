import { Component, OnInit } from '@angular/core';
import { ReportService } from '../services/report.service';

declare const Chart: any;

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html'
})
export class ReportsComponent implements OnInit {
  selectedReport: string = '';
  startDate: string = '';
  endDate: string = '';
  year: number = new Date().getFullYear();
  month: number = new Date().getMonth() + 1;
  customerId: number = 0;
  
  reportData: any = null;
  dashboardStats: any = null;
  loading: boolean = false;
  
  // Chart instances
  monthlySalesChart: any = null;
  customerPurchaseChart: any = null;
  productRevenueChart: any = null;
  gstPieChart: any = null;
  gstBarChart: any = null;

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.loadDashboardStats();
  }

  loadDashboardStats(): void {
    this.reportService.getDashboardStatistics().subscribe({
      next: (data) => {
        this.dashboardStats = data;
      },
      error: (error) => {
        console.error('Error loading dashboard stats:', error);
      }
    });
  }

  generateReport(): void {
    if (!this.selectedReport) {
      alert('Please select a report type');
      return;
    }

    this.loading = true;
    this.reportData = null;

    switch (this.selectedReport) {
      case 'monthly':
        this.generateMonthlyReport();
        break;
      case 'customer':
        this.generateCustomerReport();
        break;
      case 'product':
        this.generateProductReport();
        break;
      case 'gst':
        this.generateGSTReport();
        break;
    }
  }

  generateMonthlyReport(): void {
    this.reportService.getMonthlySalesReport(this.year, this.month).subscribe({
      next: (data) => {
        this.reportData = data;
        this.loading = false;
        setTimeout(() => this.monthlySalesChart(), 100);
      },
      error: (error) => {
        console.error('Error generating monthly report:', error);
        alert('Error generating report');
        this.loading = false;
      }
    });
  }

  generateCustomerReport(): void {
    if (!this.customerId || !this.startDate || !this.endDate) {
      alert('Please provide customer ID and date range');
      this.loading = false;
      return;
    }
    this.reportService.getCustomerWiseReport(this.customerId, this.startDate, this.endDate).subscribe({
      next: (data) => {
        // Sort bills by date descending if bills array exists
        if (data.bills && Array.isArray(data.bills)) {
          data.bills.sort((a: any, b: any) => new Date(b.billDate).getTime() - new Date(a.billDate).getTime());
        }
        this.reportData = data;
        this.loading = false;
        setTimeout(() => this.customerPurchaseChart(), 100);
      },
      error: (error) => {
        console.error('Error generating customer report:', error);
        alert('Error generating report');
        this.loading = false;
      }
    });
  }

  generateProductReport(): void {
    if (!this.startDate || !this.endDate) {
      alert('Please provide date range');
      this.loading = false;
      return;
    }
    this.reportService.getProductWiseReport(this.startDate, this.endDate).subscribe({
      next: (data) => {
        // Sort products by revenue descending
        if (data.products && Array.isArray(data.products)) {
          data.products.sort((a: any, b: any) => b.totalRevenue - a.totalRevenue);
        }
        this.reportData = data;
        this.loading = false;
        setTimeout(() => this.productRevenueChart(), 100);
      },
      error: (error) => {
        console.error('Error generating product report:', error);
        alert('Error generating report');
        this.loading = false;
      }
    });
  }

  generateGSTReport(): void {
    if (!this.startDate || !this.endDate) {
      alert('Please provide date range');
      this.loading = false;
      return;
    }
    this.reportService.getGSTReport(this.startDate, this.endDate).subscribe({
      next: (data) => {
        // Sort GST breakdown by taxable amount descending
        if (data.gstRateBreakdown && Array.isArray(data.gstRateBreakdown)) {
          data.gstRateBreakdown.sort((a: any, b: any) => b.taxableAmount - a.taxableAmount);
        }
        this.reportData = data;
        this.loading = false;
        setTimeout(() => this.gstBarChart(), 100);
      },
      error: (error) => {
        console.error('Error generating GST report:', error);
        alert('Error generating report');
        this.loading = false;
      }
    });
  }

  downloadReportPdf(): void {
    if (!this.selectedReport || !this.reportData) {
      alert('Please generate a report first before downloading');
      return;
    }

    switch (this.selectedReport) {
      case 'monthly':
        this.downloadMonthlyReportPdf();
        break;
      case 'customer':
        this.downloadCustomerReportPdf();
        break;
      case 'product':
        this.downloadProductReportPdf();
        break;
      case 'gst':
        this.downloadGSTReportPdf();
        break;
    }
  }

  downloadMonthlyReportPdf(): void {
    this.reportService.downloadMonthlySalesReportPdf(this.year, this.month).subscribe({
      next: (blob: Blob) => {
        this.downloadBlob(blob, `monthly_sales_report_${this.year}_${this.month}.pdf`);
      },
      error: (error) => {
        console.error('Error downloading PDF:', error);
        alert('Error downloading PDF. Please try again.');
      }
    });
  }

  downloadCustomerReportPdf(): void {
    this.reportService.downloadCustomerWiseReportPdf(this.customerId, this.startDate, this.endDate).subscribe({
      next: (blob: Blob) => {
        this.downloadBlob(blob, `customer_wise_report_${this.customerId}.pdf`);
      },
      error: (error) => {
        console.error('Error downloading PDF:', error);
        alert('Error downloading PDF. Please try again.');
      }
    });
  }

  downloadProductReportPdf(): void {
    this.reportService.downloadProductWiseReportPdf(this.startDate, this.endDate).subscribe({
      next: (blob: Blob) => {
        this.downloadBlob(blob, `product_wise_report_${this.startDate}_to_${this.endDate}.pdf`);
      },
      error: (error) => {
        console.error('Error downloading PDF:', error);
        alert('Error downloading PDF. Please try again.');
      }
    });
  }

  downloadGSTReportPdf(): void {
    this.reportService.downloadGSTReportPdf(this.startDate, this.endDate).subscribe({
      next: (blob: Blob) => {
        this.downloadBlob(blob, `gst_report_${this.startDate}_to_${this.endDate}.pdf`);
      },
      error: (error) => {
        console.error('Error downloading PDF:', error);
        alert('Error downloading PDF. Please try again.');
      }
    });
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}
