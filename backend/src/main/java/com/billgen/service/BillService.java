package com.billgen.service;

import com.billgen.dto.BillRequestDTO;
import com.billgen.dto.BillResponseDTO;
import com.billgen.entity.Bill;
import com.billgen.entity.BillItem;
import com.billgen.entity.Customer;
import com.billgen.entity.Product;
import com.billgen.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final AtomicInteger billCounter = new AtomicInteger(1);

    @Transactional
    public BillResponseDTO createBill(BillRequestDTO billRequest) {
        Customer customer = customerService.getCustomerById(billRequest.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Bill bill = new Bill();
        bill.setCustomer(customer);
        bill.setBillDate(billRequest.getBillDate());
        bill.setIsInterState(billRequest.getIsInterState());
        bill.setBillNumber(generateBillNumber(billRequest.getBillDate()));

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;

        for (BillRequestDTO.BillItemRequestDTO itemRequest : billRequest.getItems()) {
            Product product = productService.getProductById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequest.getProductId()));

            BillItem item = new BillItem();
            item.setBill(bill);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setGstRate(itemRequest.getGstRate());

            // Calculate line item total
            BigDecimal lineTotal = itemRequest.getQuantity().multiply(itemRequest.getUnitPrice());
            item.setTotalPrice(lineTotal);

            // Calculate GST for this item
            if (billRequest.getIsInterState()) {
                // IGST for inter-state
                BigDecimal igst = calculateGST(lineTotal, itemRequest.getGstRate());
                item.setIgstAmount(igst);
                item.setCgstAmount(BigDecimal.ZERO);
                item.setSgstAmount(BigDecimal.ZERO);
                totalIgst = totalIgst.add(igst);
            } else {
                // CGST + SGST for intra-state (split equally)
                BigDecimal totalGst = calculateGST(lineTotal, itemRequest.getGstRate());
                BigDecimal cgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                BigDecimal sgst = totalGst.subtract(cgst);
                item.setCgstAmount(cgst);
                item.setSgstAmount(sgst);
                item.setIgstAmount(BigDecimal.ZERO);
                totalCgst = totalCgst.add(cgst);
                totalSgst = totalSgst.add(sgst);
            }

            bill.addItem(item);
            subtotal = subtotal.add(lineTotal);
        }

        bill.setSubtotal(subtotal);
        bill.setCgstAmount(totalCgst);
        bill.setSgstAmount(totalSgst);
        bill.setIgstAmount(totalIgst);
        bill.setTotalAmount(subtotal.add(totalCgst).add(totalSgst).add(totalIgst));

        Bill savedBill = billRepository.save(bill);
        return convertToDTO(savedBill);
    }

    public List<BillResponseDTO> getAllBills() {
        return billRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BillResponseDTO getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        return convertToDTO(bill);
    }

    public BillResponseDTO getBillByNumber(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new RuntimeException("Bill not found with number: " + billNumber));
        return convertToDTO(bill);
    }

    public List<BillResponseDTO> getBillsByCustomerId(Long customerId) {
        return billRepository.findByCustomerIdOrderByBillDateDesc(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BillResponseDTO> getBillsByDateRange(LocalDate startDate, LocalDate endDate) {
        return billRepository.findBillsByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteBill(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        billRepository.delete(bill);
    }

    private String generateBillNumber(LocalDate billDate) {
        String datePrefix = billDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = billRepository.countBillsByDate(billDate);
        return "INV" + datePrefix + String.format("%04d", count + 1);
    }

    private BigDecimal calculateGST(BigDecimal amount, BigDecimal gstRate) {
        return amount.multiply(gstRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BillResponseDTO convertToDTO(Bill bill) {
        BillResponseDTO dto = new BillResponseDTO();
        dto.setId(bill.getId());
        dto.setBillNumber(bill.getBillNumber());
        dto.setBillDate(bill.getBillDate());
        dto.setSubtotal(bill.getSubtotal());
        dto.setCgstAmount(bill.getCgstAmount());
        dto.setSgstAmount(bill.getSgstAmount());
        dto.setIgstAmount(bill.getIgstAmount());
        dto.setTotalAmount(bill.getTotalAmount());
        dto.setPaymentStatus(bill.getPaymentStatus());
        dto.setIsInterState(bill.getIsInterState());
        dto.setCreatedAt(bill.getCreatedAt());

        // Customer DTO
        BillResponseDTO.CustomerDTO customerDTO = new BillResponseDTO.CustomerDTO();
        customerDTO.setId(bill.getCustomer().getId());
        customerDTO.setName(bill.getCustomer().getName());
        customerDTO.setAddress(bill.getCustomer().getAddress());
        customerDTO.setPhone(bill.getCustomer().getPhone());
        customerDTO.setEmail(bill.getCustomer().getEmail());
        customerDTO.setGstNumber(bill.getCustomer().getGstNumber());
        dto.setCustomer(customerDTO);

        // Bill Items DTO
        List<BillResponseDTO.BillItemResponseDTO> itemDTOs = bill.getItems().stream()
                .map(item -> {
                    BillResponseDTO.BillItemResponseDTO itemDTO = new BillResponseDTO.BillItemResponseDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setUnitPrice(item.getUnitPrice());
                    itemDTO.setGstRate(item.getGstRate());
                    itemDTO.setCgstAmount(item.getCgstAmount());
                    itemDTO.setSgstAmount(item.getSgstAmount());
                    itemDTO.setIgstAmount(item.getIgstAmount());
                    itemDTO.setTotalPrice(item.getTotalPrice());

                    BillResponseDTO.ProductDTO productDTO = new BillResponseDTO.ProductDTO();
                    productDTO.setId(item.getProduct().getId());
                    productDTO.setName(item.getProduct().getName());
                    productDTO.setDescription(item.getProduct().getDescription());
                    productDTO.setUnit(item.getProduct().getUnit());
                    productDTO.setPrice(item.getProduct().getPrice());
                    productDTO.setGstRate(item.getProduct().getGstRate());
                    productDTO.setHsnCode(item.getProduct().getHsnCode());
                    itemDTO.setProduct(productDTO);

                    return itemDTO;
                })
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }
}
