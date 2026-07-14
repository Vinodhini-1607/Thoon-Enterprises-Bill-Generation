import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Bill, BillRequest } from '../models/bill.model';

@Injectable({
  providedIn: 'root'
})
export class BillService {
  private apiUrl = 'http://localhost:8080/api/bills';

  constructor(private http: HttpClient) { }

  getBills(): Observable<Bill[]> {
    return this.http.get<Bill[]>(this.apiUrl);
  }

  getBillById(id: number): Observable<Bill> {
    return this.http.get<Bill>(`${this.apiUrl}/${id}`);
  }

  getBillByNumber(billNumber: string): Observable<Bill> {
    return this.http.get<Bill>(`${this.apiUrl}/number/${billNumber}`);
  }

  getBillsByCustomer(customerId: number): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  getBillsByDateRange(startDate: string, endDate: string): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.apiUrl}/date-range?startDate=${startDate}&endDate=${endDate}`);
  }

  createBill(billRequest: BillRequest): Observable<Bill> {
    return this.http.post<Bill>(this.apiUrl, billRequest);
  }

  deleteBill(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  downloadBillPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { responseType: 'blob' });
  }
}
