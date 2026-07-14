package com.billgen.service;

import com.billgen.dto.BillResponseDTO;
import com.billgen.entity.Bill;
import com.billgen.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BillRepository billRepository;
    private final BillService billService;

    public Map<String, Object> getMonthlySalesReport(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Bill> bills = billRepository.findBillsByDateRange(startDate, endDate);

        BigDecimal totalRevenue = bills.stream().map(Bill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCGST = bills.stream().map(Bill::getCgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSGST = bills.stream().map(Bill::getSgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIGST = bills.stream().map(Bill::getIgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new HashMap<>();
        report.put("period", startDate + " to " + endDate);
        report.put("totalBills", bills.size());
        report.put("totalRevenue", totalRevenue);
        report.put("totalCGST", totalCGST);
        report.put("totalSGST", totalSGST);
        report.put("totalIGST", totalIGST);
        
        // Statistical analysis
        if (!bills.isEmpty()) {
            BigDecimal avgBillValue = totalRevenue.divide(BigDecimal.valueOf(bills.size()), 2, BigDecimal.ROUND_HALF_UP);
            report.put("averageBillValue", avgBillValue);
            
            Optional<Bill> maxBill = bills.stream().max(Comparator.comparing(Bill::getTotalAmount));
            Optional<Bill> minBill = bills.stream().min(Comparator.comparing(Bill::getTotalAmount));
            
            maxBill.ifPresent(bill -> {
                report.put("maxBillValue", bill.getTotalAmount());
                report.put("maxBillNumber", bill.getBillNumber());
            });
            
            minBill.ifPresent(bill -> {
                report.put("minBillValue", bill.getTotalAmount());
                report.put("minBillNumber", bill.getBillNumber());
            });
            
            // Growth comparison with previous month
            LocalDate prevMonthStart = startDate.minusMonths(1);
            LocalDate prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());
            List<Bill> prevMonthBills = billRepository.findBillsByDateRange(prevMonthStart, prevMonthEnd);
            BigDecimal prevMonthRevenue = prevMonthBills.stream().map(Bill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (prevMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growthRate = totalRevenue.subtract(prevMonthRevenue)
                        .divide(prevMonthRevenue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                report.put("monthOverMonthGrowth", growthRate);
                report.put("previousMonthRevenue", prevMonthRevenue);
            }
        }
        
        report.put("bills", bills.stream().map(billService::convertToDTO).collect(Collectors.toList()));

        return report;
    }

    public Map<String, Object> getCustomerWiseReport(Long customerId, LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = billRepository.findCustomerBillsByDateRange(customerId, startDate, endDate);

        BigDecimal totalPurchase = bills.stream().map(Bill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new HashMap<>();
        report.put("customerId", customerId);
        report.put("period", startDate + " to " + endDate);
        report.put("totalBills", bills.size());
        report.put("totalPurchase", totalPurchase);
        
        // Statistical analysis
        if (!bills.isEmpty()) {
            BigDecimal avgPurchase = totalPurchase.divide(BigDecimal.valueOf(bills.size()), 2, BigDecimal.ROUND_HALF_UP);
            report.put("averagePurchase", avgPurchase);
            
            Optional<Bill> maxPurchase = bills.stream().max(Comparator.comparing(Bill::getTotalAmount));
            Optional<Bill> minPurchase = bills.stream().min(Comparator.comparing(Bill::getTotalAmount));
            
            maxPurchase.ifPresent(bill -> {
                report.put("maxPurchaseValue", bill.getTotalAmount());
                report.put("maxPurchaseDate", bill.getBillDate());
            });
            
            minPurchase.ifPresent(bill -> {
                report.put("minPurchaseValue", bill.getTotalAmount());
                report.put("minPurchaseDate", bill.getBillDate());
            });
            
            // Purchase frequency analysis
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            double frequency = (double) bills.size() / totalDays;
            report.put("purchaseFrequency", String.format("%.2f", frequency) + " bills/day");
        }
        
        report.put("bills", bills.stream().map(billService::convertToDTO).collect(Collectors.toList()));

        return report;
    }

    public Map<String, Object> getProductWiseReport(LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = billRepository.findBillsByDateRange(startDate, endDate);

        Map<Long, Map<String, Object>> productStats = new HashMap<>();

        for (Bill bill : bills) {
            for (var item : bill.getItems()) {
                Long productId = item.getProduct().getId();
                String productName = item.getProduct().getName();

                if (!productStats.containsKey(productId)) {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("productId", productId);
                    stats.put("productName", productName);
                    stats.put("totalQuantity", BigDecimal.ZERO);
                    stats.put("totalRevenue", BigDecimal.ZERO);
                    stats.put("totalGST", BigDecimal.ZERO);
                    stats.put("billCount", 0);
                    productStats.put(productId, stats);
                }

                Map<String, Object> stats = productStats.get(productId);
                BigDecimal currentQty = (BigDecimal) stats.get("totalQuantity");
                BigDecimal currentRevenue = (BigDecimal) stats.get("totalRevenue");
                BigDecimal currentGST = (BigDecimal) stats.get("totalGST");
                int currentBillCount = (int) stats.get("billCount");

                stats.put("totalQuantity", currentQty.add(item.getQuantity()));
                stats.put("totalRevenue", currentRevenue.add(item.getTotalPrice()));
                stats.put("totalGST", currentGST.add(item.getCgstAmount())
                        .add(item.getSgstAmount())
                        .add(item.getIgstAmount()));
                stats.put("billCount", currentBillCount + 1);
            }
        }

        // Add statistical analysis to each product
        BigDecimal grandTotalRevenue = productStats.values().stream()
                .map(stats -> (BigDecimal) stats.get("totalRevenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Map<String, Object> stats : productStats.values()) {
            BigDecimal productRevenue = (BigDecimal) stats.get("totalRevenue");
            if (grandTotalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal revenueShare = productRevenue.divide(grandTotalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                stats.put("revenueShare", revenueShare);
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("period", startDate + " to " + endDate);
        report.put("totalProducts", productStats.size());
        report.put("grandTotalRevenue", grandTotalRevenue);
        report.put("products", new ArrayList<>(productStats.values()));

        return report;
    }

    public Map<String, Object> getGSTReport(LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = billRepository.findBillsByDateRange(startDate, endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("period", startDate + " to " + endDate);
        report.put("totalCGST", bills.stream().map(Bill::getCgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        report.put("totalSGST", bills.stream().map(Bill::getSgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        report.put("totalIGST", bills.stream().map(Bill::getIgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        report.put("totalGST", bills.stream()
                .map(b -> b.getCgstAmount().add(b.getSgstAmount()).add(b.getIgstAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Group by GST rate
        Map<BigDecimal, Map<String, Object>> gstRateGroups = new HashMap<>();
        for (Bill bill : bills) {
            for (var item : bill.getItems()) {
                BigDecimal rate = item.getGstRate();
                if (!gstRateGroups.containsKey(rate)) {
                    Map<String, Object> group = new HashMap<>();
                    group.put("gstRate", rate);
                    group.put("taxableAmount", BigDecimal.ZERO);
                    group.put("cgstAmount", BigDecimal.ZERO);
                    group.put("sgstAmount", BigDecimal.ZERO);
                    group.put("igstAmount", BigDecimal.ZERO);
                    gstRateGroups.put(rate, group);
                }

                Map<String, Object> group = gstRateGroups.get(rate);
                BigDecimal taxable = item.getQuantity().multiply(item.getUnitPrice());
                group.put("taxableAmount", ((BigDecimal) group.get("taxableAmount")).add(taxable));
                group.put("cgstAmount", ((BigDecimal) group.get("cgstAmount")).add(item.getCgstAmount()));
                group.put("sgstAmount", ((BigDecimal) group.get("sgstAmount")).add(item.getSgstAmount()));
                group.put("igstAmount", ((BigDecimal) group.get("igstAmount")).add(item.getIgstAmount()));
            }
        }

        report.put("gstRateBreakdown", new ArrayList<>(gstRateGroups.values()));

        return report;
    }

    public Map<String, Object> getDashboardStatistics() {
        List<Bill> allBills = billRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        
        // Overall statistics
        BigDecimal totalRevenue = allBills.stream().map(Bill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCGST = allBills.stream().map(Bill::getCgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSGST = allBills.stream().map(Bill::getSgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIGST = allBills.stream().map(Bill::getIgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCGST", totalCGST);
        stats.put("totalSGST", totalSGST);
        stats.put("totalIGST", totalIGST);
        stats.put("totalGSTCollected", totalCGST.add(totalSGST).add(totalIGST));
        stats.put("totalBills", allBills.size());
        
        if (!allBills.isEmpty()) {
            // Average bill value
            BigDecimal avgBillValue = totalRevenue.divide(BigDecimal.valueOf(allBills.size()), 2, BigDecimal.ROUND_HALF_UP);
            stats.put("averageBillValue", avgBillValue);
            
            // Today's statistics
            LocalDate today = LocalDate.now();
            List<Bill> todayBills = allBills.stream()
                    .filter(bill -> bill.getBillDate().equals(today))
                    .collect(Collectors.toList());
            BigDecimal todayRevenue = todayBills.stream().map(Bill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("todayRevenue", todayRevenue);
            stats.put("todayBills", todayBills.size());
            
            // This month statistics
            LocalDate monthStart = today.withDayOfMonth(1);
            List<Bill> monthBills = allBills.stream()
                    .filter(bill -> !bill.getBillDate().isBefore(monthStart))
                    .collect(Collectors.toList());
            BigDecimal monthRevenue = monthBills.stream().map(Bill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("monthRevenue", monthRevenue);
            stats.put("monthBills", monthBills.size());
            
            // Top 5 customers by revenue
            Map<Long, BigDecimal> customerRevenue = new HashMap<>();
            for (Bill bill : allBills) {
                Long customerId = bill.getCustomer().getId();
                customerRevenue.merge(customerId, bill.getTotalAmount(), BigDecimal::add);
            }
            List<Map<String, Object>> topCustomers = customerRevenue.entrySet().stream()
                    .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> {
                        Map<String, Object> customerData = new HashMap<>();
                        customerData.put("customerId", entry.getKey());
                        customerData.put("totalRevenue", entry.getValue());
                        return customerData;
                    })
                    .collect(Collectors.toList());
            stats.put("topCustomers", topCustomers);
            
            // Top 5 products by revenue
            Map<Long, Map<String, Object>> productRevenue = new HashMap<>();
            for (Bill bill : allBills) {
                for (var item : bill.getItems()) {
                    Long productId = item.getProduct().getId();
                    String productName = item.getProduct().getName();
                    if (!productRevenue.containsKey(productId)) {
                        Map<String, Object> prodData = new HashMap<>();
                        prodData.put("productId", productId);
                        prodData.put("productName", productName);
                        prodData.put("revenue", BigDecimal.ZERO);
                        prodData.put("quantity", BigDecimal.ZERO);
                        productRevenue.put(productId, prodData);
                    }
                    Map<String, Object> prodData = productRevenue.get(productId);
                    prodData.put("revenue", ((BigDecimal) prodData.get("revenue")).add(item.getTotalPrice()));
                    prodData.put("quantity", ((BigDecimal) prodData.get("quantity")).add(item.getQuantity()));
                }
            }
            List<Map<String, Object>> topProducts = productRevenue.values().stream()
                    .sorted((a, b) -> ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")))
                    .limit(5)
                    .collect(Collectors.toList());
            stats.put("topProducts", topProducts);
            
            // Payment status breakdown
            Map<String, Long> paymentStatusCount = allBills.stream()
                    .collect(Collectors.groupingBy(
                            bill -> bill.getPaymentStatus() != null ? bill.getPaymentStatus() : "UNKNOWN",
                            Collectors.counting()
                    ));
            stats.put("paymentStatusBreakdown", paymentStatusCount);
            
            // Inter-state vs Intra-state breakdown
            long interStateCount = allBills.stream().filter(b -> Boolean.TRUE.equals(b.getIsInterState())).count();
            long intraStateCount = allBills.size() - interStateCount;
            stats.put("interStateBills", interStateCount);
            stats.put("intraStateBills", intraStateCount);
        }
        
        return stats;
    }
}
