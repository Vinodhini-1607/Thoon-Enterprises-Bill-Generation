package com.billgen.controller;

import com.billgen.service.DataExportService;
import com.billgen.service.ReportPdfGenerationService;
import com.billgen.service.ReportService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed.origins}")
public class ReportController {

    private final ReportService reportService;
    private final DataExportService dataExportService;
    private final ReportPdfGenerationService reportPdfGenerationService;

    @GetMapping("/monthly-sales")
    public ResponseEntity<Map<String, Object>> getMonthlySalesReport(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(reportService.getMonthlySalesReport(year, month));
    }

    @GetMapping("/customer-wise")
    public ResponseEntity<Map<String, Object>> getCustomerWiseReport(
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getCustomerWiseReport(customerId, startDate, endDate));
    }

    @GetMapping("/product-wise")
    public ResponseEntity<Map<String, Object>> getProductWiseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getProductWiseReport(startDate, endDate));
    }

    @GetMapping("/gst")
    public ResponseEntity<Map<String, Object>> getGSTReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getGSTReport(startDate, endDate));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        return ResponseEntity.ok(reportService.getDashboardStatistics());
    }

    @GetMapping("/export-data")
    public ResponseEntity<String> exportData() {
        String sqlData = dataExportService.exportAllData();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "billgen_data_export.sql");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(sqlData);
    }

    @GetMapping("/export/customers")
    public ResponseEntity<String> exportCustomers() {
        String sqlData = dataExportService.exportCustomers();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "customers_export.sql");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(sqlData);
    }

    @GetMapping("/export/products")
    public ResponseEntity<String> exportProducts() {
        String sqlData = dataExportService.exportProducts();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "products_export.sql");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(sqlData);
    }

    @GetMapping("/export/bills")
    public ResponseEntity<String> exportBills() {
        String sqlData = dataExportService.exportBills();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "bills_export.sql");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(sqlData);
    }

    @GetMapping("/monthly-sales/pdf")
    public ResponseEntity<byte[]> downloadMonthlySalesReportPdf(
            @RequestParam int year,
            @RequestParam int month) {
        try {
            byte[] pdfBytes = reportPdfGenerationService.generateMonthlySalesReportPdf(year, month);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "monthly_sales_report_" + year + "_" + month + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (DocumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/customer-wise/pdf")
    public ResponseEntity<byte[]> downloadCustomerWiseReportPdf(
            @RequestParam Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] pdfBytes = reportPdfGenerationService.generateCustomerWiseReportPdf(customerId, startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "customer_wise_report_" + customerId + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (DocumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/product-wise/pdf")
    public ResponseEntity<byte[]> downloadProductWiseReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] pdfBytes = reportPdfGenerationService.generateProductWiseReportPdf(startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "product_wise_report_" + startDate + "_to_" + endDate + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (DocumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/gst/pdf")
    public ResponseEntity<byte[]> downloadGSTReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] pdfBytes = reportPdfGenerationService.generateGSTReportPdf(startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "gst_report_" + startDate + "_to_" + endDate + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (DocumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
