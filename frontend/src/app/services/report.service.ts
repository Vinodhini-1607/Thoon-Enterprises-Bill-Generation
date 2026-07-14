import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) { }

  getMonthlySalesReport(year: number, month: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/monthly-sales?year=${year}&month=${month}`);
  }

  getCustomerWiseReport(customerId: number, startDate: string, endDate: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/customer-wise?customerId=${customerId}&startDate=${startDate}&endDate=${endDate}`);
  }

  getProductWiseReport(startDate: string, endDate: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/product-wise?startDate=${startDate}&endDate=${endDate}`);
  }

  getGSTReport(startDate: string, endDate: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/gst?startDate=${startDate}&endDate=${endDate}`);
  }

  getDashboardStatistics(): Observable<any> {
    return this.http.get(`${this.apiUrl}/dashboard`);
  }

  downloadMonthlySalesReportPdf(year: number, month: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/monthly-sales/pdf?year=${year}&month=${month}`, { responseType: 'blob' });
  }

  downloadCustomerWiseReportPdf(customerId: number, startDate: string, endDate: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/customer-wise/pdf?customerId=${customerId}&startDate=${startDate}&endDate=${endDate}`, { responseType: 'blob' });
  }

  downloadProductWiseReportPdf(startDate: string, endDate: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/product-wise/pdf?startDate=${startDate}&endDate=${endDate}`, { responseType: 'blob' });
  }

  downloadGSTReportPdf(startDate: string, endDate: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/gst/pdf?startDate=${startDate}&endDate=${endDate}`, { responseType: 'blob' });
  }
}
