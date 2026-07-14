package com.billgen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillRequestDTO {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Bill date is required")
    private LocalDate billDate;

    @NotNull(message = "Is inter-state is required")
    private Boolean isInterState;

    @NotNull(message = "Items are required")
    private List<BillItemRequestDTO> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillItemRequestDTO {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        private BigDecimal quantity;

        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;

        @NotNull(message = "GST rate is required")
        private BigDecimal gstRate;
    }
}
