package com.billgen.service;

import com.billgen.entity.Bill;
import com.billgen.entity.BillItem;
import com.billgen.entity.Customer;
import com.billgen.entity.Product;
import com.billgen.repository.BillItemRepository;
import com.billgen.repository.BillRepository;
import com.billgen.repository.CustomerRepository;
import com.billgen.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataExportService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String exportAllData() {
        StringBuilder sql = new StringBuilder();
        
        sql.append("-- Data Export Generated on: ").append(java.time.LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("\n\n");
        
        sql.append("-- Export Customers\n");
        sql.append(exportCustomers());
        
        sql.append("\n-- Export Products\n");
        sql.append(exportProducts());
        
        sql.append("\n-- Export Bills\n");
        sql.append(exportBills());
        
        sql.append("\n-- Export Bill Items\n");
        sql.append(exportBillItems());
        
        return sql.toString();
    }

    public String exportCustomers() {
        StringBuilder sql = new StringBuilder();
        List<Customer> customers = customerRepository.findAll();
        
        if (customers.isEmpty()) {
            sql.append("-- No customers found\n");
            return sql.toString();
        }
        
        sql.append("INSERT INTO customers (name, address, phone, email, gst_number, created_at) VALUES\n");
        
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            sql.append(String.format(
                "('%s', '%s', '%s', %s, %s, '%s')",
                escapeSql(c.getName()),
                escapeSql(c.getAddress()),
                escapeSql(c.getPhone()),
                c.getEmail() != null ? "'" + escapeSql(c.getEmail()) + "'" : "NULL",
                c.getGstNumber() != null ? "'" + escapeSql(c.getGstNumber()) + "'" : "NULL",
                c.getCreatedAt().format(TIMESTAMP_FORMATTER)
            ));
            
            if (i < customers.size() - 1) {
                sql.append(",\n");
            } else {
                sql.append(";\n");
            }
        }
        
        return sql.toString();
    }

    public String exportProducts() {
        StringBuilder sql = new StringBuilder();
        List<Product> products = productRepository.findAll();
        
        if (products.isEmpty()) {
            sql.append("-- No products found\n");
            return sql.toString();
        }
        
        sql.append("INSERT INTO products (name, description, unit, price, gst_rate, hsn_code, stock_quantity, created_at) VALUES\n");
        
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            sql.append(String.format(
                "('%s', %s, '%s', %s, %s, %s, %d, '%s')",
                escapeSql(p.getName()),
                p.getDescription() != null ? "'" + escapeSql(p.getDescription()) + "'" : "NULL",
                escapeSql(p.getUnit()),
                p.getPrice(),
                p.getGstRate(),
                p.getHsnCode() != null ? "'" + escapeSql(p.getHsnCode()) + "'" : "NULL",
                p.getStockQuantity(),
                p.getCreatedAt().format(TIMESTAMP_FORMATTER)
            ));
            
            if (i < products.size() - 1) {
                sql.append(",\n");
            } else {
                sql.append(";\n");
            }
        }
        
        return sql.toString();
    }

    public String exportBills() {
        StringBuilder sql = new StringBuilder();
        List<Bill> bills = billRepository.findAll();
        
        if (bills.isEmpty()) {
            sql.append("-- No bills found\n");
            return sql.toString();
        }
        
        sql.append("INSERT INTO bills (bill_number, customer_id, bill_date, subtotal, cgst_amount, sgst_amount, igst_amount, total_amount, payment_status, is_inter_state, created_at) VALUES\n");
        
        for (int i = 0; i < bills.size(); i++) {
            Bill b = bills.get(i);
            sql.append(String.format(
                "('%s', %d, '%s', %s, %s, %s, %s, %s, '%s', %s, '%s')",
                escapeSql(b.getBillNumber()),
                b.getCustomer().getId(),
                b.getBillDate().format(DATE_FORMATTER),
                b.getSubtotal(),
                b.getCgstAmount(),
                b.getSgstAmount(),
                b.getIgstAmount(),
                b.getTotalAmount(),
                b.getPaymentStatus() != null ? escapeSql(b.getPaymentStatus()) : "PENDING",
                b.getIsInterState() != null ? b.getIsInterState() : "FALSE",
                b.getCreatedAt().format(TIMESTAMP_FORMATTER)
            ));
            
            if (i < bills.size() - 1) {
                sql.append(",\n");
            } else {
                sql.append(";\n");
            }
        }
        
        return sql.toString();
    }

    public String exportBillItems() {
        StringBuilder sql = new StringBuilder();
        List<BillItem> billItems = billItemRepository.findAll();
        
        if (billItems.isEmpty()) {
            sql.append("-- No bill items found\n");
            return sql.toString();
        }
        
        sql.append("INSERT INTO bill_items (bill_id, product_id, quantity, unit_price, gst_rate, cgst_amount, sgst_amount, igst_amount, total_price) VALUES\n");
        
        for (int i = 0; i < billItems.size(); i++) {
            BillItem bi = billItems.get(i);
            sql.append(String.format(
                "(%d, %d, %s, %s, %s, %s, %s, %s, %s)",
                bi.getBill().getId(),
                bi.getProduct().getId(),
                bi.getQuantity(),
                bi.getUnitPrice(),
                bi.getGstRate(),
                bi.getCgstAmount(),
                bi.getSgstAmount(),
                bi.getIgstAmount(),
                bi.getTotalPrice()
            ));
            
            if (i < billItems.size() - 1) {
                sql.append(",\n");
            } else {
                sql.append(";\n");
            }
        }
        
        return sql.toString();
    }

    private String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }
}
