package com.billgen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillResponseDTO {

    private Long id;
    private String billNumber;
    private CustomerDTO customer;
    private LocalDate billDate;
    private BigDecimal subtotal;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private Boolean isInterState;
    private LocalDateTime createdAt;
    private List<BillItemResponseDTO> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDTO {
        private Long id;
        private String name;
        private String address;
        private String phone;
        private String email;
        private String gstNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillItemResponseDTO {
        private Long id;
        private ProductDTO product;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal gstRate;
        private BigDecimal cgstAmount;
        private BigDecimal sgstAmount;
        private BigDecimal igstAmount;
        private BigDecimal totalPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDTO {
        private Long id;
        private String name;
        private String description;
        private String unit;
        private BigDecimal price;
        private BigDecimal gstRate;
        private String hsnCode;
    }
}
