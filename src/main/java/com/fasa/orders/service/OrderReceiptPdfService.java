package com.fasa.orders.service;

import com.fasa.orders.entity.OrderEntity;
import com.fasa.orders.entity.OrderItemEntity;
import com.itextpdf.text.*;
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
    private static final BaseColor WHATSAPP_GREEN = new BaseColor(37, 211, 102);
    private static final BaseColor WHATSAPP_GREEN_DARK = new BaseColor(18, 140, 70);
    private static final String LOGO_CLASSPATH = "static/images/fasa-logo-remove-bg.png";

    private static final float FONT_TITLE = 10f;
    private static final float FONT_SECTION = 8f;
    private static final float FONT_BODY = 7f;
    private static final float FONT_SMALL = 6f;
    private static final float FONT_LINK = 9f;

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
        h += 54f;  // tracking box
        if (isCourierDelivery(order.getDeliveryType())) {
            h += 50f;
        }
        h += 36f;  // thank-you + WhatsApp support box
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

    private void addTrackingSection(Document document, PdfWriter writer, OrderEntity order) throws DocumentException {
        String trackUrl = buildTrackingUrl(order.getId());
        String orderRef = order.getId() != null ? String.valueOf(order.getId()) : "-";

        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100f);
        box.setSpacingAfter(4f);

        PdfPCell shell = new PdfPCell();
        shell.setBorder(Rectangle.BOX);
        shell.setBorderWidth(1.2f);
        shell.setBorderColor(BRAND_GREEN);
        shell.setBackgroundColor(BRAND_LIGHT);
        shell.setPadding(0f);

        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100f);

        inner.addCell(buildTrackingTitleBar());

        PdfPCell introCell = new PdfPCell();
        introCell.setBorder(Rectangle.NO_BORDER);
        introCell.setBackgroundColor(BRAND_LIGHT);
        introCell.setPaddingTop(7f);
        introCell.setPaddingLeft(7f);
        introCell.setPaddingRight(7f);
        introCell.setPaddingBottom(5f);
        Paragraph intro = new Paragraph();
        intro.setAlignment(Element.ALIGN_CENTER);
        intro.add(iconChunk("\u25A0", FONT_SECTION, BRAND_GREEN));
        intro.add(new Chunk("  ", bodyFont(FONT_BODY)));
        intro.add(new Chunk("Where is my order?", labelFont(FONT_SECTION)));
        intro.setSpacingAfter(3f);
        introCell.addElement(intro);
        Paragraph introHint = new Paragraph(
                "Check delivery progress anytime — it only takes a tap.",
                bodyFont(FONT_BODY));
        introHint.setAlignment(Element.ALIGN_CENTER);
        introCell.addElement(introHint);
        inner.addCell(introCell);

        inner.addCell(buildTrackingStepRow(
                "1",
                iconChunk("\u25BA", FONT_BODY, BRAND_GREEN),
                "Tap the green button below"));

        inner.addCell(buildTrackingStepRow(
                "2",
                zapfCheckIcon(8f, BRAND_GREEN),
                "See live status on your phone"));

        inner.addCell(buildTrackingButtonCell(writer, trackUrl));

        PdfPCell refCell = new PdfPCell();
        refCell.setBorder(Rectangle.NO_BORDER);
        refCell.setBackgroundColor(BRAND_LIGHT);
        refCell.setPaddingTop(4f);
        refCell.setPaddingBottom(8f);
        refCell.setPaddingLeft(6f);
        refCell.setPaddingRight(6f);
        refCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph refLine = new Paragraph();
        refLine.setAlignment(Element.ALIGN_CENTER);
        refLine.add(new Chunk("Order no: ", labelFont(FONT_SMALL)));
        Font refFont = labelFont(FONT_BODY);
        refFont.setColor(BRAND_GREEN);
        refLine.add(new Chunk(orderRef, refFont));
        refCell.addElement(refLine);
        Paragraph refHint = new Paragraph(
                "(Use this number if you contact us)",
                bodyFont(FONT_SMALL));
        refHint.setAlignment(Element.ALIGN_CENTER);
        refHint.getFont().setColor(MUTED);
        refHint.setSpacingBefore(2f);
        refCell.addElement(refHint);
        inner.addCell(refCell);

        shell.addElement(inner);
        box.addCell(shell);
        document.add(box);
    }

    private PdfPCell buildTrackingButtonCell(PdfWriter writer, String trackUrl) {
        Font btnFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, FONT_LINK, BaseColor.WHITE);

        PdfPCell buttonCell = new PdfPCell();
        buttonCell.setBorder(Rectangle.BOX);
        buttonCell.setBorderWidth(1.5f);
        buttonCell.setBorderColor(BUTTON_GREEN_DARK);
        buttonCell.setBackgroundColor(BUTTON_GREEN);
        buttonCell.setPadding(12f);
        buttonCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        buttonCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        buttonCell.setMinimumHeight(Utilities.millimetersToPoints(14f));
        buttonCell.setPaddingLeft(8f);
        buttonCell.setPaddingRight(8f);
        buttonCell.setPaddingTop(9f);
        buttonCell.setPaddingBottom(9f);
        buttonCell.setUseAscender(true);
        buttonCell.setUseDescender(true);

        Paragraph label = new Paragraph("\u25BA   TRACK MY ORDER   \u25BA", btnFont);
        label.setAlignment(Element.ALIGN_CENTER);
        buttonCell.addElement(label);

        Paragraph sub = new Paragraph("Tap here — opens in your browser", bodyFont(FONT_SMALL));
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.getFont().setColor(new BaseColor(235, 255, 220));
        sub.setSpacingBefore(3f);
        buttonCell.addElement(sub);

        buttonCell.setCellEvent(new PdfUriLinkCellEvent(writer, trackUrl));
        return buttonCell;
    }

    private PdfPCell buildTrackingTitleBar() {
        PdfPTable titleTable = new PdfPTable(2);
        titleTable.setWidthPercentage(100f);
        try {
            titleTable.setWidths(new float[]{14f, 86f});
        } catch (DocumentException ignored) {
            // keep default widths
        }

        Font whiteIcon = FontFactory.getFont(FontFactory.ZAPFDINGBATS, 12f, BaseColor.WHITE);
        PdfPCell iconCell = new PdfPCell(new Phrase(String.valueOf((char) 108), whiteIcon));
        iconCell.setBackgroundColor(BRAND_GREEN);
        iconCell.setBorder(Rectangle.NO_BORDER);
        iconCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        iconCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        iconCell.setPadding(6f);
        titleTable.addCell(iconCell);

        PdfPCell textCell = new PdfPCell(new Phrase("TRACK YOUR ORDER", labelFont(FONT_SECTION)));
        textCell.getPhrase().getFont().setColor(BaseColor.WHITE);
        textCell.setBackgroundColor(BRAND_GREEN);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setPadding(6f);
        textCell.setPaddingLeft(2f);
        titleTable.addCell(textCell);

        PdfPCell wrapper = new PdfPCell(titleTable);
        wrapper.setBorder(Rectangle.NO_BORDER);
        wrapper.setPadding(0f);
        return wrapper;
    }

    private PdfPCell buildTrackingStepRow(String stepNum, Chunk iconChunk, String text) {
        PdfPTable row = new PdfPTable(3);
        row.setWidthPercentage(100f);
        try {
            row.setWidths(new float[]{10f, 10f, 80f});
        } catch (DocumentException ignored) {
            // keep default widths
        }

        Font stepFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, BaseColor.WHITE);
        PdfPCell numCell = new PdfPCell(new Phrase(stepNum, stepFont));
        numCell.setBackgroundColor(BRAND_GREEN);
        numCell.setBorder(Rectangle.NO_BORDER);
        numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        numCell.setPadding(4f);
        row.addCell(numCell);

        Phrase iconPhrase = new Phrase();
        iconPhrase.add(iconChunk);
        PdfPCell iconCell = new PdfPCell(iconPhrase);
        iconCell.setBorder(Rectangle.NO_BORDER);
        iconCell.setBackgroundColor(BRAND_LIGHT);
        iconCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        iconCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        iconCell.setPadding(3f);
        row.addCell(iconCell);

        PdfPCell textCell = new PdfPCell(new Phrase(text, bodyFont(FONT_BODY)));
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setBackgroundColor(BRAND_LIGHT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setPadding(4f);
        textCell.setPaddingRight(6f);
        row.addCell(textCell);

        PdfPCell wrapper = new PdfPCell(row);
        wrapper.setBorder(Rectangle.NO_BORDER);
        wrapper.setBackgroundColor(BRAND_LIGHT);
        wrapper.setPadding(0f);
        return wrapper;
    }

    /**
     * Makes the whole button cell clickable without rendering default PDF link styling (blue underline).
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

    private static Chunk iconChunk(String symbol, float size, BaseColor color) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, size, color);
        return new Chunk(symbol, font);
    }

    private static Chunk zapfCheckIcon(float size, BaseColor color) {
        Font z = FontFactory.getFont(FontFactory.ZAPFDINGBATS, size, color);
        return new Chunk(String.valueOf((char) 52), z);
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

    private void addThankYouFooter(Document document, PdfWriter writer, OrderEntity order) throws DocumentException {
        Paragraph thanks = new Paragraph(
                "Thank you for choosing Fasa Products!",
                labelFont(FONT_BODY));
        thanks.setAlignment(Element.ALIGN_CENTER);
        thanks.getFont().setColor(BRAND_GREEN);
        thanks.setSpacingBefore(4f);
        thanks.setSpacingAfter(6f);
        document.add(thanks);

        document.add(buildWhatsAppSupportBox(writer, order));
    }

    private PdfPTable buildWhatsAppSupportBox(PdfWriter writer, OrderEntity order) throws DocumentException {
        String waUrl = buildWhatsAppInquiryUrl(order != null ? order.getId() : null);

        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100f);
        box.setSpacingAfter(2f);

        PdfPCell shell = new PdfPCell();
        shell.setBorder(Rectangle.BOX);
        shell.setBorderWidth(1.2f);
        shell.setBorderColor(BRAND_GREEN);
        shell.setBackgroundColor(BRAND_LIGHT);
        shell.setPadding(0f);

        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100f);

        PdfPCell titleBar = new PdfPCell(new Phrase("NEED HELP?", labelFont(FONT_SECTION)));
        titleBar.setBackgroundColor(BRAND_GREEN);
        titleBar.getPhrase().getFont().setColor(BaseColor.WHITE);
        titleBar.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleBar.setPadding(5f);
        titleBar.setBorder(Rectangle.NO_BORDER);
        inner.addCell(titleBar);

        PdfPCell hintCell = new PdfPCell(new Phrase(
                "Message us on WhatsApp — include your Order ID.",
                bodyFont(FONT_BODY)));
        hintCell.setBorder(Rectangle.NO_BORDER);
        hintCell.setBackgroundColor(BRAND_LIGHT);
        hintCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hintCell.setPaddingTop(6f);
        hintCell.setPaddingLeft(6f);
        hintCell.setPaddingRight(6f);
        hintCell.setPaddingBottom(5f);
        inner.addCell(hintCell);

        PdfPCell buttonWrap = buildWhatsAppButtonCell(writer, waUrl);
        buttonWrap.setPaddingLeft(6f);
        buttonWrap.setPaddingRight(6f);
        buttonWrap.setPaddingBottom(7f);
        inner.addCell(buttonWrap);

        shell.addElement(inner);
        box.addCell(shell);
        return box;
    }

    private PdfPCell buildWhatsAppButtonCell(PdfWriter writer, String waUrl) {
        Font btnFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, FONT_LINK, BaseColor.WHITE);

        PdfPCell buttonCell = new PdfPCell();
        buttonCell.setBorder(Rectangle.BOX);
        buttonCell.setBorderWidth(1.2f);
        buttonCell.setBorderColor(WHATSAPP_GREEN_DARK);
        buttonCell.setBackgroundColor(WHATSAPP_GREEN);
        buttonCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        buttonCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        buttonCell.setMinimumHeight(Utilities.millimetersToPoints(12f));
        buttonCell.setPaddingTop(8f);
        buttonCell.setPaddingBottom(8f);
        buttonCell.setPaddingLeft(6f);
        buttonCell.setPaddingRight(6f);

        Paragraph label = new Paragraph("MESSAGE ON WHATSAPP", btnFont);
        label.setAlignment(Element.ALIGN_CENTER);
        buttonCell.addElement(label);

        Paragraph phone = new Paragraph(applicationParameterService.getWhatsappDisplayNumber(), btnFont);
        phone.setAlignment(Element.ALIGN_CENTER);
        phone.setSpacingBefore(2f);
        buttonCell.addElement(phone);

        Paragraph sub = new Paragraph("Tap to open chat", bodyFont(FONT_SMALL));
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.getFont().setColor(new BaseColor(230, 255, 235));
        sub.setSpacingBefore(2f);
        buttonCell.addElement(sub);

        buttonCell.setCellEvent(new PdfUriLinkCellEvent(writer, waUrl));
        return buttonCell;
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

    private static String normalizeWhatsAppPhone(String raw) {
        String digits = String.valueOf(raw == null ? "" : raw).replaceAll("\\D", "");
        if (digits.startsWith("0") && digits.length() == 10) {
            return "94" + digits.substring(1);
        }
        if (digits.startsWith("94")) {
            return digits;
        }
        return StringUtils.hasText(digits) ? digits : "94767486675";
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
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, size, TEXT_DARK);
    }

    private static Font labelFont(float size) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, size, TEXT_DARK);
    }

    private static Font bodyFont(float size) {
        return FontFactory.getFont(FontFactory.HELVETICA, size, TEXT_DARK);
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
