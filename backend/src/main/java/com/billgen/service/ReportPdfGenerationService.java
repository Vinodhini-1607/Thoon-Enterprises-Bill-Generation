package com.billgen.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportPdfGenerationService {

    private final ReportService reportService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    // Dark blue color from sample bill
    private static final BaseColor DARK_BLUE = new BaseColor(26, 50, 79);
    private static final BaseColor DARK_BLUE_LIGHT = new BaseColor(42, 74, 111);

    public byte[] generateMonthlySalesReportPdf(int year, int month) throws DocumentException {
        Map<String, Object> reportData = reportService.getMonthlySalesReport(year, month);
        return generateReportPdf("Monthly Sales Report", reportData, year + "-" + month);
    }

    public byte[] generateCustomerWiseReportPdf(Long customerId, LocalDate startDate, LocalDate endDate) throws DocumentException {
        Map<String, Object> reportData = reportService.getCustomerWiseReport(customerId, startDate, endDate);
        return generateReportPdf("Customer-wise Report", reportData, "Customer " + customerId);
    }

    public byte[] generateProductWiseReportPdf(LocalDate startDate, LocalDate endDate) throws DocumentException {
        Map<String, Object> reportData = reportService.getProductWiseReport(startDate, endDate);
        return generateReportPdf("Product-wise Report", reportData, startDate + " to " + endDate);
    }

    public byte[] generateGSTReportPdf(LocalDate startDate, LocalDate endDate) throws DocumentException {
        Map<String, Object> reportData = reportService.getGSTReport(startDate, endDate);
        return generateReportPdf("GST Report", reportData, startDate + " to " + endDate);
    }

    private byte[] generateReportPdf(String reportTitle, Map<String, Object> reportData, String period) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Company Header
        Font companyFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
        
        Paragraph companyName = new Paragraph("THOON ENTERPRISES", companyFont);
        companyName.setAlignment(Element.ALIGN_CENTER);
        document.add(companyName);
        
        Paragraph companySubtitle = new Paragraph("Construction Materials Supplier", subtitleFont);
        companySubtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(companySubtitle);
        
        document.add(Chunk.NEWLINE);
        
        // Report Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Paragraph title = new Paragraph(reportTitle, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(Chunk.NEWLINE);

        // Period
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        
        Paragraph periodPara = new Paragraph("Period: " + period, normalFont);
        document.add(periodPara);
        document.add(Chunk.NEWLINE);

        // Generate report-specific content
        generateReportContent(document, reportTitle, reportData, boldFont, normalFont);

        // Footer
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
        Paragraph footer = new Paragraph("THOON ENTERPRISES | Construction Materials Supplier | Generated on: " + 
                LocalDate.now().format(DATE_FORMATTER), footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        return outputStream.toByteArray();
    }

    private void generateReportContent(Document document, String reportTitle, Map<String, Object> reportData, 
            Font boldFont, Font normalFont) throws DocumentException {
        
        switch (reportTitle) {
            case "Monthly Sales Report":
                generateMonthlySalesContent(document, reportData, boldFont, normalFont);
                break;
            case "Customer-wise Report":
                generateCustomerWiseContent(document, reportData, boldFont, normalFont);
                break;
            case "Product-wise Report":
                generateProductWiseContent(document, reportData, boldFont, normalFont);
                break;
            case "GST Report":
                generateGSTContent(document, reportData, boldFont, normalFont);
                break;
        }
    }

    private void generateMonthlySalesContent(Document document, Map<String, Object> reportData, 
            Font boldFont, Font normalFont) throws DocumentException {
        
        // Summary statistics
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        
        addCell(summaryTable, "Total Bills:", String.valueOf(reportData.getOrDefault("totalBills", 0)), boldFont, normalFont);
        addCell(summaryTable, "Average Bill Value:", "₹" + formatNumber(reportData.get("averageBillValue")), boldFont, normalFont);
        
        document.add(summaryTable);
        document.add(Chunk.NEWLINE);
    }

    private void generateCustomerWiseContent(Document document, Map<String, Object> reportData, 
            Font boldFont, Font normalFont) throws DocumentException {
        
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        
        addCell(summaryTable, "Total Bills:", String.valueOf(reportData.getOrDefault("totalBills", 0)), boldFont, normalFont);
        addCell(summaryTable, "Average Purchase:", "₹" + formatNumber(reportData.get("averagePurchase")), boldFont, normalFont);
        addCell(summaryTable, "Purchase Frequency:", String.valueOf(reportData.getOrDefault("purchaseFrequency", 0)), boldFont, normalFont);
        
        document.add(summaryTable);
        document.add(Chunk.NEWLINE);
    }

    private void generateProductWiseContent(Document document, Map<String, Object> reportData, 
            Font boldFont, Font normalFont) throws DocumentException {
        
        // Summary
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        
        addCell(summaryTable, "Total Products:", String.valueOf(reportData.getOrDefault("totalProducts", 0)), boldFont, normalFont);
        addCell(summaryTable, "Grand Total Revenue:", "₹" + formatNumber(reportData.get("grandTotalRevenue")), boldFont, normalFont);
        
        document.add(summaryTable);
        document.add(Chunk.NEWLINE);

        // Products table
        if (reportData.containsKey("products")) {
            PdfPTable productsTable = new PdfPTable(6);
            productsTable.setWidthPercentage(100);
            productsTable.setWidths(new float[]{3, 1, 2, 2, 1, 1});

            addTableHeaderDarkBlue(productsTable, "Product", boldFont);
            addTableHeaderDarkBlue(productsTable, "Quantity", boldFont);
            addTableHeaderDarkBlue(productsTable, "Revenue", boldFont);
            addTableHeaderDarkBlue(productsTable, "Total GST", boldFont);
            addTableHeaderDarkBlue(productsTable, "Bills", boldFont);
            addTableHeaderDarkBlue(productsTable, "Share %", boldFont);

            java.util.List<Map<String, Object>> products = (java.util.List<Map<String, Object>>) reportData.get("products");
            for (Map<String, Object> product : products) {
                productsTable.addCell(new Phrase(String.valueOf(product.get("productName")), normalFont));
                productsTable.addCell(new Phrase(String.valueOf(product.get("totalQuantity")), normalFont));
                productsTable.addCell(new Phrase("₹" + formatNumber(product.get("totalRevenue")), normalFont));
                productsTable.addCell(new Phrase("₹" + formatNumber(product.get("totalGST")), normalFont));
                productsTable.addCell(new Phrase(String.valueOf(product.get("billCount")), normalFont));
                productsTable.addCell(new Phrase(formatNumber(product.get("revenueShare")) + "%", normalFont));
            }

            document.add(productsTable);
        }
    }

    private void generateGSTContent(Document document, Map<String, Object> reportData, 
            Font boldFont, Font normalFont) throws DocumentException {
        
        // GST breakdown table
        if (reportData.containsKey("gstRateBreakdown")) {
            PdfPTable gstTable = new PdfPTable(5);
            gstTable.setWidthPercentage(100);
            gstTable.setWidths(new float[]{1, 2, 2, 2, 2});

            addTableHeaderDarkBlue(gstTable, "GST Rate", boldFont);
            addTableHeaderDarkBlue(gstTable, "Taxable Amount", boldFont);
            addTableHeaderDarkBlue(gstTable, "CGST", boldFont);
            addTableHeaderDarkBlue(gstTable, "SGST", boldFont);
            addTableHeaderDarkBlue(gstTable, "IGST", boldFont);

            java.util.List<Map<String, Object>> gstBreakdown = (java.util.List<Map<String, Object>>) reportData.get("gstRateBreakdown");
            for (Map<String, Object> gst : gstBreakdown) {
                gstTable.addCell(new Phrase(String.valueOf(gst.get("gstRate")) + "%", normalFont));
                gstTable.addCell(new Phrase("₹" + formatNumber(gst.get("taxableAmount")), normalFont));
                gstTable.addCell(new Phrase("₹" + formatNumber(gst.get("cgstAmount")), normalFont));
                gstTable.addCell(new Phrase("₹" + formatNumber(gst.get("sgstAmount")), normalFont));
                gstTable.addCell(new Phrase("₹" + formatNumber(gst.get("igstAmount")), normalFont));
            }

            document.add(gstTable);
        }
    }

    private void addCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTableHeaderDarkBlue(PdfPTable table, String header, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(header, font));
        cell.setBackgroundColor(DARK_BLUE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);
    }

    private String formatNumber(Object value) {
        if (value == null) return "0.00";
        if (value instanceof Number) {
            return String.format("%.2f", ((Number) value).doubleValue());
        }
        return String.valueOf(value);
    }
}
