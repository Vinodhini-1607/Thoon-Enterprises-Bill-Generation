export interface BillItem {
  id?: number;
  productId: number;
  quantity: number;
  unitPrice: number;
  gstRate: number;
  product?: Product;
  cgstAmount?: number;
  sgstAmount?: number;
  igstAmount?: number;
  totalPrice?: number;
}

export interface Bill {
  id?: number;
  billNumber?: string;
  customerId: number;
  customer?: Customer;
  billDate: string;
  subtotal?: number;
  cgstAmount?: number;
  sgstAmount?: number;
  igstAmount?: number;
  totalAmount?: number;
  paymentStatus?: string;
  isInterState: boolean;
  items: BillItem[];
  createdAt?: string;
}

export interface BillRequest {
  customerId: number;
  billDate: string;
  isInterState: boolean;
  items: BillItem[];
}

export interface Product {
  id?: number;
  name: string;
  description?: string;
  unit: string;
  price: number;
  gstRate: number;
  hsnCode?: string;
  stockQuantity?: number;
  createdAt?: string;
}

export interface Customer {
  id?: number;
  name: string;
  address: string;
  phone: string;
  email?: string;
  gstNumber?: string;
  createdAt?: string;
}
