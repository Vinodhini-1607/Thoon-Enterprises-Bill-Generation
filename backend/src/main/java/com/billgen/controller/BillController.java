package com.billgen.controller;

import com.billgen.dto.BillRequestDTO;
import com.billgen.dto.BillResponseDTO;
import com.billgen.service.BillService;
import com.billgen.service.PdfGenerationService;
import com.itextpdf.text.DocumentException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed.origins}")
public class BillController {

    private final BillService billService;
    private final PdfGenerationService pdfGenerationService;

    @GetMapping
    public ResponseEntity<List<BillResponseDTO>> getAllBills() {
        return ResponseEntity.ok(billService.getAllBills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDTO> getBillById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(billService.getBillById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/number/{billNumber}")
    public ResponseEntity<BillResponseDTO> getBillByNumber(@PathVariable String billNumber) {
        try {
            return ResponseEntity.ok(billService.getBillByNumber(billNumber));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BillResponseDTO>> getBillsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(billService.getBillsByCustomerId(customerId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<BillResponseDTO>> getBillsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(billService.getBillsByDateRange(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<BillResponseDTO> createBill(@Valid @RequestBody BillRequestDTO billRequest) {
        BillResponseDTO createdBill = billService.createBill(billRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBill);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        try {
            billService.deleteBill(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadBillPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = pdfGenerationService.generateBillPdf(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "bill_" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
