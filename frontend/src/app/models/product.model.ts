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
