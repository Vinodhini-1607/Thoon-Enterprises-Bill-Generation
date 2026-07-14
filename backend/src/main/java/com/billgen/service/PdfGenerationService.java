package com.billgen.service;

import com.billgen.dto.BillResponseDTO;
import com.billgen.entity.Bill;
import com.billgen.entity.BillItem;
import com.billgen.repository.BillRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private final BillRepository billRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    
    // Dark blue color from sample bill
    private static final BaseColor DARK_BLUE = new BaseColor(26, 50, 79);
    private static final BaseColor DARK_BLUE_LIGHT = new BaseColor(42, 74, 111);
    
    // Company details (placeholders - update with actual values)
    private static final String COMPANY_NAME = "THOON ENTERPRISES";
    private static final String COMPANY_ADDRESS = "123, Main Street, Chennai - 600001";
    private static final String COMPANY_GSTIN = "33AABCU9603R1ZN";
    private static final String COMPANY_PHONE = "+91 9876543210";

    public byte[] generateBillPdf(Long billId) throws DocumentException, IOException {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Company Header with Logo - Side by side layout
        Font companyFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, DARK_BLUE);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        
        // Create header table with logo on left, company info on right
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 3});
        
        // Logo cell
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(5);
        try {
            String logoPath = getClass().getClassLoader().getResource("images/logo.png").getPath();
            Image logo = Image.getInstance(logoPath);
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_LEFT);
            logoCell.addElement(logo);
        } catch (Exception e) {
            // Logo not found, add placeholder
            logoCell.addElement(new Paragraph("LOGO", companyFont));
        }
        
        // Company info cell
        PdfPCell companyInfoCell = new PdfPCell();
        companyInfoCell.setBorder(Rectangle.NO_BORDER);
        companyInfoCell.setPadding(5);
        companyInfoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        companyInfoCell.addElement(new Paragraph(COMPANY_NAME, companyFont));
        companyInfoCell.addElement(Chunk.NEWLINE);
        companyInfoCell.addElement(new Paragraph(COMPANY_ADDRESS, subtitleFont));
        companyInfoCell.addElement(new Paragraph("GSTIN: " + COMPANY_GSTIN, subtitleFont));
        companyInfoCell.addElement(new Paragraph("Phone: " + COMPANY_PHONE, subtitleFont));
        
        headerTable.addCell(logoCell);
        headerTable.addCell(companyInfoCell);
        document.add(headerTable);
        
        // Add a line separator
        PdfPTable lineTable = new PdfPTable(1);
        lineTable.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorder(Rectangle.BOTTOM);
        lineCell.setBorderColor(DARK_BLUE);
        lineCell.setBorderWidth(2);
        lineCell.setFixedHeight(10);
        lineTable.addCell(lineCell);
        document.add(lineTable);
        
        document.add(Chunk.NEWLINE);
        
        // Invoice Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, DARK_BLUE);
        Paragraph title = new Paragraph("TAX INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Invoice Details Table with better styling
        PdfPTable billInfoTable = new PdfPTable(4);
        billInfoTable.setWidthPercentage(100);
        billInfoTable.setWidths(new float[]{1.5f, 2, 1.5f, 2});
        billInfoTable.setSpacingBefore(10);
        billInfoTable.setSpacingAfter(10);

        addStyledCell(billInfoTable, "Invoice No.:", bill.getBillNumber(), boldFont, normalFont);
        addStyledCell(billInfoTable, "Dated:", bill.getBillDate().format(DATE_FORMATTER), boldFont, normalFont);
        addStyledCell(billInfoTable, "Place of Supply:", "Tamil Nadu", boldFont, normalFont);
        addStyledCell(billInfoTable, "Reverse Charge:", "No", boldFont, normalFont);
        addStyledCell(billInfoTable, "Transport Mode:", "Road", boldFont, normalFont);
        addStyledCell(billInfoTable, "Vehicle No.:", "-", boldFont, normalFont);
        addStyledCell(billInfoTable, "Station:", "Chennai", boldFont, normalFont);
        addStyledCell(billInfoTable, "E-Way Bill No.:", "-", boldFont, normalFont);

        document.add(billInfoTable);

        // Billed To and Shipped To sections with better styling
        PdfPTable customerSectionsTable = new PdfPTable(2);
        customerSectionsTable.setWidthPercentage(100);
        customerSectionsTable.setSpacingBefore(15);
        customerSectionsTable.setSpacingAfter(15);
        
        // Billed To
        PdfPCell billedToCell = new PdfPCell();
        billedToCell.setBorder(Rectangle.BOX);
        billedToCell.setBorderColor(DARK_BLUE);
        billedToCell.setBorderWidth(1);
        billedToCell.setPadding(10);
        billedToCell.setBackgroundColor(new BaseColor(245, 247, 250));
        billedToCell.addElement(new Paragraph("Billed To:", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, DARK_BLUE)));
        billedToCell.addElement(Chunk.NEWLINE);
        billedToCell.addElement(new Paragraph(bill.getCustomer().getName(), new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)));
        billedToCell.addElement(new Paragraph(bill.getCustomer().getAddress(), normalFont));
        billedToCell.addElement(new Paragraph("Phone: " + bill.getCustomer().getPhone(), normalFont));
        if (bill.getCustomer().getGstNumber() != null) {
            billedToCell.addElement(new Paragraph("GSTIN: " + bill.getCustomer().getGstNumber(), normalFont));
        }
        
        // Shipped To (same as billed to for now)
        PdfPCell shippedToCell = new PdfPCell();
        shippedToCell.setBorder(Rectangle.BOX);
        shippedToCell.setBorderColor(DARK_BLUE);
        shippedToCell.setBorderWidth(1);
        shippedToCell.setPadding(10);
        shippedToCell.setBackgroundColor(new BaseColor(245, 247, 250));
        shippedToCell.addElement(new Paragraph("Shipped To:", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, DARK_BLUE)));
        shippedToCell.addElement(Chunk.NEWLINE);
        shippedToCell.addElement(new Paragraph(bill.getCustomer().getName(), new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)));
        shippedToCell.addElement(new Paragraph(bill.getCustomer().getAddress(), normalFont));
        shippedToCell.addElement(new Paragraph("Phone: " + bill.getCustomer().getPhone(), normalFont));
        if (bill.getCustomer().getGstNumber() != null) {
            shippedToCell.addElement(new Paragraph("GSTIN: " + bill.getCustomer().getGstNumber(), normalFont));
        }
        
        customerSectionsTable.addCell(billedToCell);
        customerSectionsTable.addCell(shippedToCell);
        document.add(customerSectionsTable);

        // Goods Table with better styling
        Paragraph itemsTitle = new Paragraph("Goods Details:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, DARK_BLUE));
        itemsTitle.setSpacingBefore(10);
        document.add(itemsTitle);

        PdfPTable itemsTable = new PdfPTable(8);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{0.5f, 3, 1.5f, 1, 1.5f, 1, 1, 1.5f});
        itemsTable.setSpacingBefore(5);
        itemsTable.setSpacingAfter(15);

        // Header with dark blue background
        addTableHeaderDarkBlue(itemsTable, "S.N", boldFont);
        addTableHeaderDarkBlue(itemsTable, "DESCRIPTION OF GOODS", boldFont);
        addTableHeaderDarkBlue(itemsTable, "HSN/SAC", boldFont);
        addTableHeaderDarkBlue(itemsTable, "QTY/UNIT", boldFont);
        addTableHeaderDarkBlue(itemsTable, "PRICE", boldFont);
        addTableHeaderDarkBlue(itemsTable, "CGST", boldFont);
        addTableHeaderDarkBlue(itemsTable, "SGST", boldFont);
        addTableHeaderDarkBlue(itemsTable, "AMOUNT (₹)", boldFont);

        // Items with alternating row colors
        int serialNumber = 1;
        for (BillItem item : bill.getItems()) {
            BaseColor rowColor = (serialNumber % 2 == 0) ? new BaseColor(245, 247, 250) : BaseColor.WHITE;
            
            PdfPCell snCell = new PdfPCell(new Phrase(String.valueOf(serialNumber++), normalFont));
            snCell.setBackgroundColor(rowColor);
            snCell.setBorderColor(DARK_BLUE);
            snCell.setBorderWidth(0.5f);
            snCell.setPadding(6);
            snCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(snCell);
            
            PdfPCell descCell = new PdfPCell(new Phrase(item.getProduct().getName(), normalFont));
            descCell.setBackgroundColor(rowColor);
            descCell.setBorderColor(DARK_BLUE);
            descCell.setBorderWidth(0.5f);
            descCell.setPadding(6);
            itemsTable.addCell(descCell);
            
            PdfPCell hsnCell = new PdfPCell(new Phrase(item.getProduct().getHsnCode() != null ? item.getProduct().getHsnCode() : "-", normalFont));
            hsnCell.setBackgroundColor(rowColor);
            hsnCell.setBorderColor(DARK_BLUE);
            hsnCell.setBorderWidth(0.5f);
            hsnCell.setPadding(6);
            hsnCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(hsnCell);
            
            PdfPCell qtyCell = new PdfPCell(new Phrase(item.getQuantity() + " " + item.getProduct().getUnit(), normalFont));
            qtyCell.setBackgroundColor(rowColor);
            qtyCell.setBorderColor(DARK_BLUE);
            qtyCell.setBorderWidth(0.5f);
            qtyCell.setPadding(6);
            qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(qtyCell);
            
            PdfPCell priceCell = new PdfPCell(new Phrase("₹" + item.getUnitPrice(), normalFont));
            priceCell.setBackgroundColor(rowColor);
            priceCell.setBorderColor(DARK_BLUE);
            priceCell.setBorderWidth(0.5f);
            priceCell.setPadding(6);
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            itemsTable.addCell(priceCell);
            
            PdfPCell cgstCell = new PdfPCell(new Phrase("₹" + item.getCgstAmount(), normalFont));
            cgstCell.setBackgroundColor(rowColor);
            cgstCell.setBorderColor(DARK_BLUE);
            cgstCell.setBorderWidth(0.5f);
            cgstCell.setPadding(6);
            cgstCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            itemsTable.addCell(cgstCell);
            
            PdfPCell sgstCell = new PdfPCell(new Phrase("₹" + item.getSgstAmount(), normalFont));
            sgstCell.setBackgroundColor(rowColor);
            sgstCell.setBorderColor(DARK_BLUE);
            sgstCell.setBorderWidth(0.5f);
            sgstCell.setPadding(6);
            sgstCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            itemsTable.addCell(sgstCell);
            
            PdfPCell amountCell = new PdfPCell(new Phrase("₹" + item.getTotalPrice(), normalFont));
            amountCell.setBackgroundColor(rowColor);
            amountCell.setBorderColor(DARK_BLUE);
            amountCell.setBorderWidth(0.5f);
            amountCell.setPadding(6);
            amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            itemsTable.addCell(amountCell);
        }

        document.add(itemsTable);

        // Tax Breakdown Table with better styling
        Paragraph taxTitle = new Paragraph("Tax Breakdown:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, DARK_BLUE));
        taxTitle.setSpacingBefore(10);
        document.add(taxTitle);

        PdfPTable taxTable = new PdfPTable(5);
        taxTable.setWidthPercentage(100);
        taxTable.setWidths(new float[]{2, 2, 2, 2, 2});
        taxTable.setSpacingBefore(5);
        taxTable.setSpacingAfter(15);

        addTableHeaderDarkBlue(taxTable, "TAX RATE", boldFont);
        addTableHeaderDarkBlue(taxTable, "TAXABLE AMT.", boldFont);
        addTableHeaderDarkBlue(taxTable, "CGST AMT.", boldFont);
        addTableHeaderDarkBlue(taxTable, "SGST AMT.", boldFont);
        addTableHeaderDarkBlue(taxTable, "TOTAL TAX", boldFont);

        // Group by GST rate
        java.util.Map<Double, Double[]> taxBreakdown = new java.util.HashMap<>();
        for (BillItem item : bill.getItems()) {
            double gstRate = item.getGstRate().doubleValue();
            double taxableAmount =  item.getUnitPrice().doubleValue() * item.getQuantity().doubleValue();
            double cgstAmount = item.getCgstAmount().doubleValue();
            double sgstAmount = item.getSgstAmount().doubleValue();
            double totalTax = cgstAmount + sgstAmount;

            if (taxBreakdown.containsKey(gstRate)) {
                Double[] existing = taxBreakdown.get(gstRate);
                existing[0] += taxableAmount;
                existing[1] += cgstAmount;
                existing[2] += sgstAmount;
                existing[3] += totalTax;
            } else {
                taxBreakdown.put(gstRate, new Double[]{taxableAmount, cgstAmount, sgstAmount, totalTax});
            }
        }

        int taxRow = 1;
        for (java.util.Map.Entry<Double, Double[]> entry : taxBreakdown.entrySet()) {
            BaseColor rowColor = (taxRow % 2 == 0) ? new BaseColor(245, 247, 250) : BaseColor.WHITE;
            
            PdfPCell rateCell = new PdfPCell(new Phrase(entry.getKey() + "% (" + (entry.getKey() / 2) + "% CGST + " + (entry.getKey() / 2) + "% SGST)", normalFont));
            rateCell.setBackgroundColor(rowColor);
            rateCell.setBorderColor(DARK_BLUE);
            rateCell.setBorderWidth(0.5f);
            rateCell.setPadding(6);
            taxTable.addCell(rateCell);
            
            PdfPCell taxableCell = new PdfPCell(new Phrase("₹" + String.format("%.2f", entry.getValue()[0]), normalFont));
            taxableCell.setBackgroundColor(rowColor);
            taxableCell.setBorderColor(DARK_BLUE);
            taxableCell.setBorderWidth(0.5f);
            taxableCell.setPadding(6);
            taxableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            taxTable.addCell(taxableCell);
            
            PdfPCell cgstCell = new PdfPCell(new Phrase("₹" + String.format("%.2f", entry.getValue()[1]), normalFont));
            cgstCell.setBackgroundColor(rowColor);
            cgstCell.setBorderColor(DARK_BLUE);
            cgstCell.setBorderWidth(0.5f);
            cgstCell.setPadding(6);
            cgstCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            taxTable.addCell(cgstCell);
            
            PdfPCell sgstCell = new PdfPCell(new Phrase("₹" + String.format("%.2f", entry.getValue()[2]), normalFont));
            sgstCell.setBackgroundColor(rowColor);
            sgstCell.setBorderColor(DARK_BLUE);
            sgstCell.setBorderWidth(0.5f);
            sgstCell.setPadding(6);
            sgstCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            taxTable.addCell(sgstCell);
            
            PdfPCell totalTaxCell = new PdfPCell(new Phrase("₹" + String.format("%.2f", entry.getValue()[3]), normalFont));
            totalTaxCell.setBackgroundColor(rowColor);
            totalTaxCell.setBorderColor(DARK_BLUE);
            totalTaxCell.setBorderWidth(0.5f);
            totalTaxCell.setPadding(6);
            totalTaxCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            taxTable.addCell(totalTaxCell);
            
            taxRow++;
        }

        document.add(taxTable);

        // Total Invoice Value with dark blue background
        PdfPTable totalTable = new PdfPTable(1);
        totalTable.setWidthPercentage(100);
        totalTable.setSpacingBefore(15);
        totalTable.setSpacingAfter(15);
        
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.WHITE);
        PdfPCell totalCell = new PdfPCell(new Phrase("Total Invoice Value (Inclusive of GST): ₹ " + String.format("%.2f", bill.getTotalAmount()), totalFont));
        totalCell.setBackgroundColor(DARK_BLUE);
        totalCell.setBorderColor(DARK_BLUE);
        totalCell.setPadding(15);
        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalCell.setBorder(Rectangle.NO_BORDER);
        
        totalTable.addCell(totalCell);
        document.add(totalTable);

        // Rupees in Words
        Font wordsFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, DARK_BLUE);
        Paragraph rupeesWords = new Paragraph("Rupees in Words: " + convertToRupeesWords(bill.getTotalAmount().doubleValue()), wordsFont);
        rupeesWords.setSpacingBefore(10);
        rupeesWords.setSpacingAfter(10);
        document.add(rupeesWords);

        // Bank Details
        Paragraph bankTitle = new Paragraph("Bank Details:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, DARK_BLUE));
        bankTitle.setSpacingBefore(15);
        document.add(bankTitle);
        
        Font bankFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Paragraph bankDetails = new Paragraph("[ Bank details intentionally left blank as requested ]", bankFont);
        bankDetails.setSpacingAfter(15);
        document.add(bankDetails);

        // Terms and Conditions
        Paragraph termsTitle = new Paragraph("TERMS & CONDITIONS:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, DARK_BLUE));
        termsTitle.setSpacingBefore(15);
        document.add(termsTitle);
        
        Font termsFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
        Paragraph terms = new Paragraph("1. E. & O.E.\n2. Goods once sold will not be taken back.\n3. Interest @ 18% p.a. will be charged if the payment is not made within the stipulated time.\n4. Subject to 'Tamil Nadu' Jurisdiction only.", termsFont);
        terms.setSpacingBefore(5);
        terms.setSpacingAfter(20);
        document.add(terms);

        // Signature section
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setSpacingBefore(20);
        
        PdfPCell receiverCell = new PdfPCell(new Phrase("Receiver's Signature", new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL)));
        receiverCell.setBorder(Rectangle.TOP);
        receiverCell.setBorderColor(DARK_BLUE);
        receiverCell.setBorderWidth(1);
        receiverCell.setPadding(15);
        receiverCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        PdfPCell authCell = new PdfPCell(new Phrase("For " + COMPANY_NAME + "\n\nAuthorised Signatory", new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL)));
        authCell.setBorder(Rectangle.TOP);
        authCell.setBorderColor(DARK_BLUE);
        authCell.setBorderWidth(1);
        authCell.setPadding(15);
        authCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        signatureTable.addCell(receiverCell);
        signatureTable.addCell(authCell);
        document.add(signatureTable);

        // Footer with company details
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        
        // Add line separator before footer
        PdfPTable footerLineTable = new PdfPTable(1);
        footerLineTable.setWidthPercentage(100);
        PdfPCell footerLineCell = new PdfPCell();
        footerLineCell.setBorder(Rectangle.TOP);
        footerLineCell.setBorderColor(DARK_BLUE);
        footerLineCell.setBorderWidth(2);
        footerLineCell.setFixedHeight(10);
        footerLineTable.addCell(footerLineCell);
        document.add(footerLineTable);
        
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, DARK_BLUE);
        Paragraph footer = new Paragraph(COMPANY_NAME + " | Construction Materials Supplier | Phone: " + COMPANY_PHONE + " | Email: info@thoonenterprises.com", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        document.add(footer);
        
        Paragraph generated = new Paragraph("Generated on: " + bill.getCreatedAt().format(TIMESTAMP_FORMATTER), 
                new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY));
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingBefore(5);
        document.add(generated);

        document.close();

        return outputStream.toByteArray();
    }

    private String convertToRupeesWords(double amount) {
        // Simple implementation - can be enhanced with proper number to words conversion
        long rupees = (long) amount;
        int paise = (int) Math.round((amount - rupees) * 100);
        
        String words = "Rupees " + convertNumberToWords(rupees);
        if (paise > 0) {
            words += " and " + paise + " Paisa";
        }
        words += " Only";
        return words;
    }

    private String convertNumberToWords(long number) {
        // Simplified number to words conversion
        if (number == 0) return "Zero";
        
        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
        String[] teens = {"Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        
        if (number < 10) return units[(int) number];
        if (number < 20) return teens[(int) (number - 10)];
        if (number < 100) return tens[(int) (number / 10)] + ((number % 10 != 0) ? " " + units[(int) (number % 10)] : "");
        if (number < 1000) return units[(int) (number / 100)] + " Hundred" + ((number % 100 != 0) ? " " + convertNumberToWords(number % 100) : "");
        if (number < 100000) return convertNumberToWords(number / 1000) + " Thousand" + ((number % 1000 != 0) ? " " + convertNumberToWords(number % 1000) : "");
        if (number < 10000000) return convertNumberToWords(number / 100000) + " Lakh" + ((number % 100000 != 0) ? " " + convertNumberToWords(number % 100000) : "");
        return convertNumberToWords(number / 10000000) + " Crore" + ((number % 10000000 != 0) ? " " + convertNumberToWords(number % 10000000) : "");
    }

    private void addCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addStyledCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setBackgroundColor(new BaseColor(245, 247, 250));
        labelCell.setPadding(5);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setBackgroundColor(BaseColor.WHITE);
        valueCell.setPadding(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String header, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(header, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableHeaderDarkBlue(PdfPTable table, String header, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
        cell.setBackgroundColor(DARK_BLUE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);
        cell.setBorderColor(DARK_BLUE);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }
}
