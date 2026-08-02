package com.fasa.orders.service;

import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.entity.OrderItemEntity;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class OrderReceiptPdfService {

    private static final BaseColor BRAND_GREEN = new BaseColor(74, 124, 42);
    private static final BaseColor BUTTON_GREEN = new BaseColor(98, 168, 52);
    private static final BaseColor BUTTON_GREEN_DARK = new BaseColor(56, 98, 28);
    private static final BaseColor BRAND_LIGHT = new BaseColor(232, 245, 224);
    private static final BaseColor TEXT_DARK = new BaseColor(33, 48, 28);
    private static final BaseColor MUTED = new BaseColor(90, 106, 82);
    private static final BaseColor COURIER_BG = new BaseColor(255, 249, 230);
    private static final BaseColor COURIER_BORDER = new BaseColor(180, 140, 40);

    private static final String COMPANY_NAME = "AnyTech Solution Private Limited";
    private static final String COMPANY_WHATSAPP = "+94 70 660 3503";
    private static final String COMPANY_WHATSAPP_URL = "https://wa.me/94706603503";
    private static final String COMPANY_WEBSITE = "https://anytech.lk/";
    private static final BaseColor FOOTER_MUTED = new BaseColor(90, 98, 104);
    private static final BaseColor TRACK_TEXT = new BaseColor(33, 37, 41);

    private static final String LOGO_CLASSPATH = "static/images/fasa-logo-remove-bg.png";
    private static final String TRACK_STATUS_IMAGE_CLASSPATH = "static/images/your-order-status.png";
    private static final String TRACK_BUTTON_IMAGE_CLASSPATH = "static/images/track-live-status-button.png";
    private static final String THANK_YOU_WHATSAPP_IMAGE_CLASSPATH = "static/images/thank-you-whatsapp.png";
    /* Same family as dashboard/login HTML (Poppins) */
    private static final String FONT_REGULAR_CLASSPATH = "fonts/Poppins-Regular.ttf";
    private static final String FONT_BOLD_CLASSPATH = "fonts/Poppins-Bold.ttf";

    private static volatile BaseFont poppinsRegular;
    private static volatile BaseFont poppinsBold;

    private static final float FONT_TITLE = 10f;
    private static final float FONT_SECTION = 8f;
    private static final float FONT_BODY = 7f;
    private static final float FONT_SMALL = 6f;

    private final ApplicationParameterService applicationParameterService;
    private final float paperWidthMm;
    private final float paperMinHeightMm;
    private final float paperMaxHeightMm;
    private final float marginMm;

    public OrderReceiptPdfService(
            ApplicationParameterService applicationParameterService,
            @Value("${fasa.orders.receipt.paper-width-mm:80}") float paperWidthMm,
            @Value("${fasa.orders.receipt.paper-min-height-mm:55}") float paperMinHeightMm,
            @Value("${fasa.orders.receipt.paper-max-height-mm:600}") float paperMaxHeightMm) {
        this.applicationParameterService = applicationParameterService;
        this.paperWidthMm = paperWidthMm > 0 ? paperWidthMm : 80f;
        this.paperMinHeightMm = paperMinHeightMm > 0 ? paperMinHeightMm : 55f;
        this.paperMaxHeightMm = paperMaxHeightMm > 0 ? paperMaxHeightMm : 600f;
        this.marginMm = 3f;
    }

    public byte[] generateReceiptPdf(OrderEntity order) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = createPosDocument(order);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();

        addLogo(document);
        addSeparator(document);
        addHeaderBlock(document, order);
        addSeparator(document);
        addItemsSection(document, order);
        addSeparator(document);
        addTotalsTable(document, order);
        addSeparator(document);
        addTrackingSection(document, writer, order);
        if (isCourierDelivery(order.getDeliveryType())) {
            addSeparator(document);
            addCourierPaymentSection(document, order);
        }
        addSeparator(document);
        addThankYouFooter(document, writer, order);
        addBarcodeAndCompanyFooter(document, writer, order);

        document.close();
        return outputStream.toByteArray();
    }

    private Document createPosDocument(OrderEntity order) {
        float widthPt = Utilities.millimetersToPoints(paperWidthMm);
        float heightMm = estimateContentHeightMm(order);
        float heightPt = Utilities.millimetersToPoints(heightMm);
        float marginPt = Utilities.millimetersToPoints(marginMm);
        return new Document(new Rectangle(widthPt, heightPt), marginPt, marginPt, marginPt, marginPt);
    }

    private float estimateContentHeightMm(OrderEntity order) {
        int itemCount = order.getItems() == null ? 0 : order.getItems().size();
        int metaRows = 2;
        if (StringUtils.hasText(order.getDeliveryType())) {
            metaRows++;
        }
        if (StringUtils.hasText(order.getPlacedAt())) {
            metaRows++;
        }

        float h = marginMm * 2f;
        h += 24f;  // logo
        h += 5f * 6f; // separators (approx.)
        h += 10f;  // receipt title
        h += metaRows * 6f;
        h += 10f;  // items section title + table header
        if (itemCount == 0) {
            h += 5f;
        } else {
            h += itemCount * 13f;
        }
        h += 22f;  // totals
        h += 58f;  // order status image + compact CTA + order no
        if (isCourierDelivery(order.getDeliveryType())) {
            h += 50f;
        }
        h += 40f;  // thank-you WhatsApp image
        h += 22f;  // barcode + company footer
        h += 6f;   // safety buffer

        return Math.min(Math.max(h, paperMinHeightMm), paperMaxHeightMm);
    }

    private void addLogo(Document document) throws DocumentException, IOException {
        ClassPathResource logoResource = new ClassPathResource(LOGO_CLASSPATH);
        if (!logoResource.exists()) {
            Paragraph brand = new Paragraph("FASA PRODUCTS", titleFont(FONT_TITLE));
            brand.setAlignment(Element.ALIGN_CENTER);
            brand.setSpacingAfter(6f);
            document.add(brand);
            return;
        }
        try (InputStream in = logoResource.getInputStream()) {
            Image logo = Image.getInstance(readAllBytes(in));
            float maxLogoWidth = Utilities.millimetersToPoints(paperWidthMm - 10f);
            logo.scaleToFit(maxLogoWidth, Utilities.millimetersToPoints(26f));
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingAfter(6f);
            document.add(logo);
        }
    }

    private void addHeaderBlock(Document document, OrderEntity order) throws DocumentException {
        Paragraph receiptTitle = new Paragraph("ORDER RECEIPT", titleFont(FONT_TITLE));
        receiptTitle.setAlignment(Element.ALIGN_CENTER);
        receiptTitle.getFont().setColor(BRAND_GREEN);
        receiptTitle.setSpacingAfter(6f);
        document.add(receiptTitle);

        PdfPTable meta = new PdfPTable(2);
        meta.setWidthPercentage(100f);
        meta.setWidths(new float[]{36f, 64f});
        meta.setSpacingAfter(4f);

        addMetaRow(meta, "Order ID", String.valueOf(order.getId()));
        addMetaRow(meta, "Customer", nullToDash(order.getCustomerName()));
        if (StringUtils.hasText(order.getDeliveryType())) {
            addMetaRow(meta, "Delivery", order.getDeliveryType().trim());
        }
        if (StringUtils.hasText(order.getPlacedAt())) {
            DateTimeFormatter outputFormat =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            String formattedDate = Instant
                    .parse(order.getPlacedAt().trim())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(outputFormat);

            addMetaRow(meta, "Placed", formattedDate);
        }

        document.add(meta);
    }

    private void addItemsSection(Document document, OrderEntity order) throws DocumentException {
        Paragraph section = sectionHeading("Order items");
        document.add(section);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{40f, 12f, 24f, 24f});
        table.setSpacingAfter(4f);

        addTableHeader(table, "Item");
        addTableHeader(table, "Qty");
        addTableHeader(table, "Price");
        addTableHeader(table, "Total");

        if (order.getItems() == null || order.getItems().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No line items.", bodyFont(FONT_BODY)));
            empty.setColspan(4);
            empty.setPadding(5f);
            empty.setBackgroundColor(BRAND_LIGHT);
            empty.setBorderColor(BRAND_LIGHT);
            table.addCell(empty);
        } else {
            for (OrderItemEntity item : order.getItems()) {
                int qty = item.getQuantity() == null ? 1 : Math.max(1, item.getQuantity());
                BigDecimal unit = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
                BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(qty));

                table.addCell(bodyCell(item.getName()));
                table.addCell(bodyCellCenter(String.valueOf(qty)));
                table.addCell(bodyCellRight(formatRs(unit)));
                table.addCell(bodyCellRight(formatRs(lineTotal)));
            }
        }

        document.add(table);
    }

    private void addTotalsTable(Document document, OrderEntity order) throws DocumentException {
        BigDecimal subtotal = order.getOrderPrice() == null ? BigDecimal.ZERO : order.getOrderPrice();
        BigDecimal delivery = order.getDeliveryPrice() == null ? BigDecimal.ZERO : order.getDeliveryPrice();
        BigDecimal total = subtotal.add(delivery).setScale(2, RoundingMode.HALF_UP);

        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(100f);
        totals.setWidths(new float[]{58f, 42f});
        totals.setSpacingAfter(4f);

        addTotalRow(totals, "Items subtotal", formatRs(subtotal), false);
        addTotalRow(totals, "Delivery", formatRs(delivery), false);
        addTotalRow(totals, "TOTAL", formatRs(total), true);

        document.add(totals);
    }

    private void addTrackingSection(Document document, PdfWriter writer, OrderEntity order)
            throws DocumentException, IOException {
        String trackUrl = buildTrackingUrl(order.getId());
        String orderRef = order.getId() != null ? String.valueOf(order.getId()) : "-";

        PdfPTable section = new PdfPTable(1);
        section.setWidthPercentage(100f);
        section.setSpacingAfter(2f);

        // Full status graphic (title + icons + labels)
        section.addCell(buildOrderStatusImageCell());

        // Clickable track button image → live tracking URL
        PdfPCell buttonWrap = new PdfPCell();
        buttonWrap.setBorder(Rectangle.NO_BORDER);
        buttonWrap.setHorizontalAlignment(Element.ALIGN_CENTER);
        buttonWrap.setPaddingTop(6f);
        buttonWrap.setPaddingBottom(3f);
        buttonWrap.setPaddingLeft(0f);
        buttonWrap.setPaddingRight(0f);
        buttonWrap.addElement(buildTrackingButtonTable(writer, trackUrl));
        section.addCell(buttonWrap);

        // Order number footer
        Phrase refPhrase = new Phrase();
        refPhrase.add(new Chunk("ORDER NO: ",
                receiptFont(FONT_SMALL, true, TRACK_TEXT)));
        refPhrase.add(new Chunk(orderRef,
                receiptFont(FONT_BODY, true, TRACK_TEXT)));
        PdfPCell refCell = new PdfPCell(refPhrase);
        refCell.setBorder(Rectangle.NO_BORDER);
        refCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        refCell.setPaddingTop(4f);
        refCell.setPaddingBottom(2f);
        section.addCell(refCell);

        document.add(section);
    }

    private PdfPCell buildOrderStatusImageCell() throws DocumentException, IOException {
        ClassPathResource imageResource = new ClassPathResource(TRACK_STATUS_IMAGE_CLASSPATH);
        if (!imageResource.exists()) {
            throw new IOException("Required order status image missing: " + TRACK_STATUS_IMAGE_CLASSPATH);
        }

        try (InputStream in = imageResource.getInputStream()) {
            Image statusImage = Image.getInstance(readAllBytes(in));
            float maxWidth = Utilities.millimetersToPoints(paperWidthMm - (marginMm * 2f) - 1f);
            float maxHeight = Utilities.millimetersToPoints(42f);
            statusImage.scaleToFit(maxWidth, maxHeight);
            statusImage.setAlignment(Element.ALIGN_CENTER);

            PdfPCell imageCell = new PdfPCell(statusImage, true);
            imageCell.setBorder(Rectangle.NO_BORDER);
            imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            imageCell.setPaddingTop(2f);
            imageCell.setPaddingBottom(2f);
            imageCell.setPaddingLeft(0f);
            imageCell.setPaddingRight(0f);
            return imageCell;
        }
    }

    /**
     * Track Live Status image button — whole image is a link to the track-order page.
     */
    private PdfPTable buildTrackingButtonTable(PdfWriter writer, String trackUrl)
            throws DocumentException, IOException {
        ClassPathResource imageResource = new ClassPathResource(TRACK_BUTTON_IMAGE_CLASSPATH);
        if (!imageResource.exists()) {
            throw new IOException("Required track button image missing: " + TRACK_BUTTON_IMAGE_CLASSPATH);
        }

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(78f);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);

        try (InputStream in = imageResource.getInputStream()) {
            Image buttonImage = Image.getInstance(readAllBytes(in));
            float maxWidth = Utilities.millimetersToPoints((paperWidthMm - (marginMm * 2f)) * 0.78f);
            float maxHeight = Utilities.millimetersToPoints(12f);
            buttonImage.scaleToFit(maxWidth, maxHeight);
            buttonImage.setAlignment(Element.ALIGN_CENTER);

            PdfPCell pill = new PdfPCell(buttonImage, true);
            pill.setBorder(Rectangle.NO_BORDER);
            pill.setHorizontalAlignment(Element.ALIGN_CENTER);
            pill.setVerticalAlignment(Element.ALIGN_MIDDLE);
            pill.setPadding(0f);
            pill.setCellEvent(new PdfUriLinkCellEvent(writer, trackUrl));
            table.addCell(pill);
        }
        return table;
    }

    /**
     * Makes the whole cell clickable without default PDF link styling (blue underline).
     */
    private static final class PdfUriLinkCellEvent implements PdfPCellEvent {
        private final PdfWriter writer;
        private final String url;

        private PdfUriLinkCellEvent(PdfWriter writer, String url) {
            this.writer = writer;
            this.url = url;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            if (!StringUtils.hasText(url) || writer == null || position == null) {
                return;
            }
            PdfAnnotation link = PdfAnnotation.createLink(
                    writer,
                    position,
                    PdfAnnotation.HIGHLIGHT_PUSH,
                    new PdfAction(url));
            writer.addAnnotation(link);
        }
    }

    private void addCourierPaymentSection(Document document, OrderEntity order) throws DocumentException {
        BigDecimal subtotal = order.getOrderPrice() == null ? BigDecimal.ZERO : order.getOrderPrice();
        BigDecimal delivery = order.getDeliveryPrice() == null ? BigDecimal.ZERO : order.getDeliveryPrice();
        BigDecimal total = subtotal.add(delivery).setScale(2, RoundingMode.HALF_UP);

        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100f);
        box.setSpacingAfter(4f);

        PdfPCell shell = new PdfPCell();
        shell.setBorder(Rectangle.BOX);
        shell.setBorderColor(COURIER_BORDER);
        shell.setBackgroundColor(COURIER_BG);
        shell.setPadding(8f);

        Paragraph heading = new Paragraph("BANK TRANSFER (COURIER)", labelFont(FONT_SECTION));
        heading.setAlignment(Element.ALIGN_CENTER);
        heading.setSpacingAfter(4f);
        shell.addElement(heading);

        Paragraph instruction = new Paragraph(
                "Please transfer " + formatRs(total) + ". Use Order ID "
                        + order.getId() + " as the payment reference.",
                bodyFont(FONT_SMALL));
        instruction.setAlignment(Element.ALIGN_CENTER);
        instruction.setSpacingAfter(6f);
        shell.addElement(instruction);

        PdfPTable bank = new PdfPTable(2);
        bank.setWidthPercentage(100f);
        bank.setWidths(new float[]{38f, 62f});
        addMetaRow(bank, "Account", applicationParameterService.getBankAccountName());
        addMetaRow(bank, "Number", applicationParameterService.getBankAccountNumber());
        addMetaRow(bank, "Bank", applicationParameterService.getBankLabel());
        addMetaRow(bank, "Branch", applicationParameterService.getBankBranch());
        shell.addElement(bank);

        box.addCell(shell);
        document.add(box);
    }

    private void addThankYouFooter(Document document, PdfWriter writer, OrderEntity order)
            throws DocumentException, IOException {
        document.add(buildThankYouWhatsAppImage(writer, order));
    }

    /**
     * Thank-you + Need Help WhatsApp graphic — whole image opens WhatsApp chat.
     */
    private PdfPTable buildThankYouWhatsAppImage(PdfWriter writer, OrderEntity order)
            throws DocumentException, IOException {
        ClassPathResource imageResource = new ClassPathResource(THANK_YOU_WHATSAPP_IMAGE_CLASSPATH);
        if (!imageResource.exists()) {
            throw new IOException("Required thank-you image missing: " + THANK_YOU_WHATSAPP_IMAGE_CLASSPATH);
        }

        String waUrl = buildWhatsAppInquiryUrl(order != null ? order.getId() : null);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100f);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(2f);
        table.setSpacingAfter(2f);

        try (InputStream in = imageResource.getInputStream()) {
            Image helpImage = Image.getInstance(readAllBytes(in));
            float maxWidth = Utilities.millimetersToPoints(paperWidthMm - (marginMm * 2f) - 1f);
            float maxHeight = Utilities.millimetersToPoints(36f);
            helpImage.scaleToFit(maxWidth, maxHeight);
            helpImage.setAlignment(Element.ALIGN_CENTER);

            PdfPCell cell = new PdfPCell(helpImage, true);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(0f);
            cell.setCellEvent(new PdfUriLinkCellEvent(writer, waUrl));
            table.addCell(cell);
        }
        return table;
    }

    /**
     * Compact barcode + powered-by company strip at the very bottom.
     */
    private void addBarcodeAndCompanyFooter(Document document, PdfWriter writer, OrderEntity order)
            throws DocumentException {
        addSeparator(document);

        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100f);
        footer.setSpacingBefore(1f);
        footer.setSpacingAfter(1f);

        // Compact order barcode
        Image barcodeImage = buildOrderBarcodeImage(writer, order);
        if (barcodeImage != null) {
            PdfPCell barcodeCell = new PdfPCell(barcodeImage, false);
            barcodeCell.setBorder(Rectangle.NO_BORDER);
            barcodeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            barcodeCell.setPaddingTop(2f);
            barcodeCell.setPaddingBottom(2f);
            footer.addCell(barcodeCell);
        }

        // Small company details
        Paragraph developedBy = new Paragraph(
                "Software developed by",
                receiptFont(4.8f, false, FOOTER_MUTED));
        developedBy.setAlignment(Element.ALIGN_CENTER);
        developedBy.setLeading(6f);
        PdfPCell developedByCell = new PdfPCell();
        developedByCell.setBorder(Rectangle.NO_BORDER);
        developedByCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        developedByCell.setPaddingTop(2f);
        developedByCell.setPaddingBottom(0.5f);
        developedByCell.addElement(developedBy);
        footer.addCell(developedByCell);

        Paragraph company = new Paragraph(COMPANY_NAME, receiptFont(5.5f, true, FOOTER_MUTED));
        company.setAlignment(Element.ALIGN_CENTER);
        company.setLeading(7f);
        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        companyCell.setPaddingTop(0.5f);
        companyCell.setPaddingBottom(0.5f);
        companyCell.addElement(company);
        footer.addCell(companyCell);

        Phrase waPhrase = new Phrase();
        waPhrase.add(new Chunk("WhatsApp: ", receiptFont(5f, false, FOOTER_MUTED)));
        Chunk waLink = new Chunk(COMPANY_WHATSAPP, receiptFont(5f, true, BRAND_GREEN));
        waLink.setAnchor(COMPANY_WHATSAPP_URL);
        waPhrase.add(waLink);
        PdfPCell waCell = new PdfPCell(waPhrase);
        waCell.setBorder(Rectangle.NO_BORDER);
        waCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        waCell.setPaddingTop(0.5f);
        waCell.setPaddingBottom(0.5f);
        footer.addCell(waCell);

        Phrase webPhrase = new Phrase();
        webPhrase.add(new Chunk("Visit us: ", receiptFont(5f, false, FOOTER_MUTED)));
        Chunk webLink = new Chunk(COMPANY_WEBSITE, receiptFont(5f, true, BRAND_GREEN));
        webLink.setAnchor(COMPANY_WEBSITE);
        webPhrase.add(webLink);
        PdfPCell webCell = new PdfPCell(webPhrase);
        webCell.setBorder(Rectangle.NO_BORDER);
        webCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        webCell.setPaddingTop(0.5f);
        webCell.setPaddingBottom(2f);
        footer.addCell(webCell);

        document.add(footer);
    }

    private Image buildOrderBarcodeImage(PdfWriter writer, OrderEntity order) {
        if (writer == null || order == null || order.getId() == null) {
            return null;
        }
        try {
            Barcode128 barcode = new Barcode128();
            barcode.setCodeType(Barcode128.CODE128);
            barcode.setCode(String.valueOf(order.getId()));
            barcode.setFont(null); // keep footer compact — no text under bars
            barcode.setBarHeight(Utilities.millimetersToPoints(8f));
            barcode.setX(0.7f);
            barcode.setN(1.6f);
            Image image = barcode.createImageWithBarcode(writer.getDirectContent(), BaseColor.BLACK, BaseColor.BLACK);
            float maxWidth = Utilities.millimetersToPoints(paperWidthMm - (marginMm * 2f) - 8f);
            if (image.getScaledWidth() > maxWidth) {
                image.scaleToFit(maxWidth, Utilities.millimetersToPoints(10f));
            }
            image.setAlignment(Element.ALIGN_CENTER);
            return image;
        } catch (Exception ex) {
            return null;
        }
    }

    private String buildWhatsAppInquiryUrl(Long orderId) {
        String text = "Hello, I have a question about my order.";
        if (orderId != null) {
            text += " Order ID: " + orderId;
        }
        try {
            return "https://wa.me/" + applicationParameterService.getWhatsappPhoneNumber() + "?text=" + URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return "https://wa.me/" + applicationParameterService.getWhatsappPhoneNumber();
        }
    }

    private void addSeparator(Document document) throws DocumentException {
        Paragraph line = new Paragraph("--------------------------------", bodyFont(FONT_SMALL));
        line.getFont().setColor(BRAND_GREEN);
        line.setAlignment(Element.ALIGN_CENTER);
        line.setSpacingBefore(3f);
        line.setSpacingAfter(3f);
        document.add(line);
    }

    private static void addMetaRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont(FONT_BODY)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3f);
        labelCell.setBackgroundColor(BRAND_LIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, bodyFont(FONT_BODY)));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3f);
        table.addCell(valueCell);
    }

    private static void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, labelFont(FONT_BODY)));
        cell.setBackgroundColor(BRAND_GREEN);
        cell.getPhrase().getFont().setColor(BaseColor.WHITE);
        cell.setPadding(4f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static PdfPCell bodyCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(nullToDash(text), bodyFont(FONT_BODY)));
        cell.setPadding(4f);
        cell.setBorderColor(BRAND_LIGHT);
        return cell;
    }

    private static PdfPCell bodyCellCenter(String text) {
        PdfPCell cell = bodyCell(text);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private static PdfPCell bodyCellRight(String text) {
        PdfPCell cell = bodyCell(text);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private static void addTotalRow(PdfPTable table, String label, String value, boolean emphasize) {
        Font labelF = emphasize ? labelFont(FONT_SECTION) : bodyFont(FONT_BODY);
        Font valueF = emphasize ? labelFont(FONT_SECTION) : bodyFont(FONT_BODY);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelF));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3f);
        if (emphasize) {
            labelCell.setBackgroundColor(BRAND_LIGHT);
        }
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueF));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (emphasize) {
            valueCell.setBackgroundColor(BRAND_LIGHT);
        }
        table.addCell(valueCell);
    }

    private static Paragraph sectionHeading(String text) {
        Paragraph p = new Paragraph(text, labelFont(FONT_SECTION));
        p.getFont().setColor(BRAND_GREEN);
        p.setSpacingBefore(2f);
        p.setSpacingAfter(4f);
        return p;
    }

    private String buildTrackingUrl(Long orderId) {
        if (orderId == null) {
            return applicationParameterService.getStoreFrontBaseUrl() + "/track-order";
        }
        return applicationParameterService.getStoreFrontBaseUrl() + "/track-order?token=" + orderId;
    }

    private static boolean isCourierDelivery(String deliveryType) {
        return "courier".equalsIgnoreCase(String.valueOf(deliveryType == null ? "" : deliveryType).trim());
    }

    private static String formatRs(BigDecimal amount) {
        BigDecimal safe = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "Rs. " + df.format(safe);
    }

    private static String nullToDash(String value) {
        return StringUtils.hasText(value) ? value.trim() : "-";
    }

    private static Font titleFont(float size) {
        return receiptFont(size, true, TEXT_DARK);
    }

    private static Font labelFont(float size) {
        return receiptFont(size, true, TEXT_DARK);
    }

    private static Font bodyFont(float size) {
        return receiptFont(size, false, TEXT_DARK);
    }

    /** Poppins — same family as HTML dashboard/login pages. */
    private static Font receiptFont(float size, boolean bold, BaseColor color) {
        try {
            BaseFont base = bold ? poppinsBold() : poppinsRegular();
            return new Font(base, size, Font.NORMAL, color != null ? color : TEXT_DARK);
        } catch (Exception ex) {
            return FontFactory.getFont(
                    bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
                    size,
                    color != null ? color : TEXT_DARK);
        }
    }

    private static BaseFont poppinsRegular() throws DocumentException, IOException {
        BaseFont cached = poppinsRegular;
        if (cached != null) {
            return cached;
        }
        synchronized (OrderReceiptPdfService.class) {
            if (poppinsRegular == null) {
                poppinsRegular = loadEmbeddedFont(FONT_REGULAR_CLASSPATH, "Poppins-Regular.ttf");
            }
            return poppinsRegular;
        }
    }

    private static BaseFont poppinsBold() throws DocumentException, IOException {
        BaseFont cached = poppinsBold;
        if (cached != null) {
            return cached;
        }
        synchronized (OrderReceiptPdfService.class) {
            if (poppinsBold == null) {
                poppinsBold = loadEmbeddedFont(FONT_BOLD_CLASSPATH, "Poppins-Bold.ttf");
            }
            return poppinsBold;
        }
    }

    private static BaseFont loadEmbeddedFont(String classpath, String logicalName)
            throws DocumentException, IOException {
        ClassPathResource resource = new ClassPathResource(classpath);
        if (!resource.exists()) {
            throw new IOException("Font missing on classpath: " + classpath);
        }
        try (InputStream in = resource.getInputStream()) {
            byte[] bytes = readAllBytes(in);
            return BaseFont.createFont(
                    logicalName,
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    bytes,
                    null);
        }
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) {
            return "";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }
}
