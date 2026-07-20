package uy.washop.notification.application;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uy.washop.config.AppProperties;
import uy.washop.order.domain.Order;
import uy.washop.order.domain.OrderItem;
import uy.washop.seo.application.SeoUrlService;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.settings.infrastructure.SiteSettingsRepository;

/** Sends order-confirmation emails to the customer and to the shop owner. Never blocks order processing on failure. */
@Service
public class OrderEmailService {

    private static final Logger log = LoggerFactory.getLogger(OrderEmailService.class);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Montevideo"));

    private final JavaMailSender mailSender;
    private final boolean configured;
    private final String fromAddress;
    private final AppProperties appProperties;
    private final SiteSettingsRepository siteSettingsRepository;
    private final SeoUrlService seoUrlService;

    public OrderEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            AppProperties appProperties,
            SiteSettingsRepository siteSettingsRepository,
            SeoUrlService seoUrlService
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.configured = this.mailSender != null && StringUtils.hasText(appProperties.getMail().getFrom());
        this.fromAddress = appProperties.getMail().getFrom();
        this.appProperties = appProperties;
        this.siteSettingsRepository = siteSettingsRepository;
        this.seoUrlService = seoUrlService;
    }

    public void sendOrderConfirmedEmails(Order order, List<OrderItem> items) {
        if (!configured) {
            log.warn("Mail is not configured (MAIL_HOST missing) — skipping order confirmation emails");
            return;
        }

        SiteSettings settings = siteSettingsRepository.findById(SiteSettings.DEFAULT_ID)
                .or(() -> siteSettingsRepository.findAll().stream().findFirst())
                .orElse(null);
        String businessName = settings != null && StringUtils.hasText(settings.getBusinessName())
                ? settings.getBusinessName()
                : "WA Shop";

        String customerEmail = order.getCustomer() != null ? order.getCustomer().getEmail() : null;
        if (StringUtils.hasText(customerEmail)) {
            trySend(customerEmail, "Confirmamos tu pedido " + orderCode(order) + " — " + businessName,
                    buildCustomerHtml(order, items, businessName, settings));
        }

        String ownerEmail = settings != null ? settings.getContactEmail() : null;
        if (StringUtils.hasText(ownerEmail)) {
            trySend(ownerEmail, "Nuevo pedido pagado " + orderCode(order) + " — " + businessName,
                    buildOwnerHtml(order, items, businessName));
        }
    }

    private void trySend(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setFrom(fromAddress);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | RuntimeException ex) {
            log.error("Failed to send order email to {}: {}", to, ex.getMessage());
        }
    }

    static String orderCode(Order order) {
        return "#" + order.getId().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String buildCustomerHtml(Order order, List<OrderItem> items, String businessName, SiteSettings settings) {
        String customerName = order.getCustomer() != null ? order.getCustomer().getName() : "";
        String rows = itemRows(items, order);
        String address = StringUtils.hasText(order.getShippingAddress())
                ? "<p style=\"margin:0 0 4px;color:#475569;\"><strong>Dirección de envío:</strong> "
                        + escape(order.getShippingAddress()) + "</p>"
                : "";
        String whatsappSection = buildWhatsappSection(order, businessName, settings);

        return wrapper(businessName, """
                <h1 style="margin:0 0 8px;font-size:22px;color:#0f172a;">¡Gracias por tu compra, %s!</h1>
                <p style="margin:0 0 20px;color:#475569;">Tu pago fue confirmado. Acá tenés el resumen de tu pedido.</p>
                %s
                %s
                %s
                """.formatted(escape(customerName), orderSummaryBlock(order, rows), address, whatsappSection));
    }

    /** Prompts the customer to reach out first — the shop coordinates delivery by replying to their message. */
    private String buildWhatsappSection(Order order, String businessName, SiteSettings settings) {
        String phone = settings != null ? normalizePhone(settings.getWhatsappNumber()) : null;
        if (phone == null) {
            return """
                    <p style="margin:20px 0 0;color:#475569;">
                      Escribinos por WhatsApp con tu número de pedido para coordinar la entrega. ¡Gracias por elegirnos!
                    </p>
                    """;
        }
        String message = "Hola! Realicé el pedido " + orderCode(order) + " en " + businessName
                + " y quiero coordinar la entrega.";
        String whatsappUrl = "https://wa.me/" + phone + "?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

        return """
                <p style="margin:20px 0 12px;color:#475569;">
                  Para coordinar la entrega, escribinos por WhatsApp confirmando tu número de pedido
                  <strong>%s</strong>.
                </p>
                <p style="margin:0;">
                  <a href="%s" style="background:#25d366;color:#ffffff;text-decoration:none;padding:10px 18px;border-radius:8px;font-weight:600;display:inline-block;">Escribir por WhatsApp</a>
                </p>
                """.formatted(orderCode(order), whatsappUrl);
    }

    private static String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }

    private String buildOwnerHtml(Order order, List<OrderItem> items, String businessName) {
        String customerName = order.getCustomer() != null ? order.getCustomer().getName() : "—";
        String customerPhone = order.getCustomer() != null ? order.getCustomer().getPhone() : "—";
        String customerEmail = order.getCustomer() != null ? order.getCustomer().getEmail() : null;
        String rows = itemRows(items, order);
        String adminUrl = seoUrlService.resolvePublicSiteUrl() + "/admin/pedidos/" + order.getId();

        return wrapper(businessName, """
                <h1 style="margin:0 0 8px;font-size:22px;color:#0f172a;">Nuevo pedido pagado</h1>
                <p style="margin:0 0 20px;color:#475569;">Se confirmó el pago de un pedido en %s.</p>
                <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:16px;margin:0 0 20px;">
                  <p style="margin:0 0 4px;"><strong>Cliente:</strong> %s</p>
                  <p style="margin:0 0 4px;"><strong>Teléfono:</strong> %s</p>
                  <p style="margin:0;"><strong>Email:</strong> %s</p>
                </div>
                %s
                <p style="margin:24px 0 0;">
                  <a href="%s" style="background:#2563eb;color:#ffffff;text-decoration:none;padding:10px 18px;border-radius:8px;font-weight:600;display:inline-block;">Ver pedido en el panel</a>
                </p>
                """.formatted(
                escape(businessName),
                escape(customerName),
                escape(customerPhone),
                customerEmail != null ? escape(customerEmail) : "—",
                orderSummaryBlock(order, rows),
                adminUrl
        ));
    }

    private String orderSummaryBlock(Order order, String rows) {
        String currency = order.getCurrency().name();
        StringBuilder discountRows = new StringBuilder();
        if (order.getPromotionDiscount() != null && order.getPromotionDiscount().signum() > 0) {
            discountRows.append(discountRow("Descuento por promoción", order.getPromotionDiscount(), currency));
        }
        if (order.getCouponDiscount() != null && order.getCouponDiscount().signum() > 0) {
            String label = StringUtils.hasText(order.getDiscountCode())
                    ? "Código " + order.getDiscountCode()
                    : "Código de descuento";
            discountRows.append(discountRow(label, order.getCouponDiscount(), currency));
        }
        if (!discountRows.isEmpty()) {
            discountRows.append("""
                    <tr>
                      <td colspan="2" style="padding:6px 16px;color:#64748b;border-top:1px solid #e2e8f0;">Subtotal</td>
                      <td style="padding:6px 16px;text-align:right;color:#64748b;border-top:1px solid #e2e8f0;">%s</td>
                    </tr>
                    """.formatted(formatMoney(order.getSubtotal(), currency)));
        }

        return """
                <div style="background:#ffffff;border:1px solid #e2e8f0;border-radius:10px;overflow:hidden;margin:0 0 16px;">
                  <div style="background:#f1f5f9;padding:12px 16px;font-size:13px;color:#64748b;">
                    Pedido %s &middot; %s
                  </div>
                  <table style="width:100%%;border-collapse:collapse;">
                    <thead>
                      <tr style="text-align:left;font-size:12px;color:#94a3b8;text-transform:uppercase;">
                        <th style="padding:10px 16px;">Producto</th>
                        <th style="padding:10px 16px;">Cant.</th>
                        <th style="padding:10px 16px;text-align:right;">Subtotal</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                    <tfoot>
                      %s
                      <tr>
                        <td colspan="2" style="padding:12px 16px;font-weight:700;border-top:1px solid #e2e8f0;">Total</td>
                        <td style="padding:12px 16px;text-align:right;font-weight:700;border-top:1px solid #e2e8f0;">%s</td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
                """.formatted(
                orderCode(order),
                DATE_FORMAT.format(order.getCreatedAt()),
                rows,
                discountRows.toString(),
                formatMoney(order.getTotal(), currency)
        );
    }

    private String discountRow(String label, BigDecimal amount, String currency) {
        return """
                <tr>
                  <td colspan="2" style="padding:6px 16px;color:#16a34a;border-top:1px solid #e2e8f0;">%s</td>
                  <td style="padding:6px 16px;text-align:right;color:#16a34a;border-top:1px solid #e2e8f0;">−%s</td>
                </tr>
                """.formatted(escape(label), formatMoney(amount, currency));
    }

    private String itemRows(List<OrderItem> items, Order order) {
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : items) {
            sb.append("""
                    <tr>
                      <td style="padding:8px 16px;border-top:1px solid #f1f5f9;">%s</td>
                      <td style="padding:8px 16px;border-top:1px solid #f1f5f9;">%d</td>
                      <td style="padding:8px 16px;border-top:1px solid #f1f5f9;text-align:right;">%s</td>
                    </tr>
                    """.formatted(
                    escape(item.getProductName()),
                    item.getQuantity(),
                    formatMoney(item.getSubtotal(), order.getCurrency().name())
            ));
        }
        return sb.toString();
    }

    private String wrapper(String businessName, String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <body style="margin:0;padding:24px;background:#f8fafc;font-family:Segoe UI,Arial,sans-serif;">
                  <div style="max-width:520px;margin:0 auto;background:#ffffff;border-radius:14px;padding:28px;border:1px solid #e2e8f0;">
                    <p style="margin:0 0 20px;font-weight:700;font-size:15px;color:#2563eb;letter-spacing:-0.02em;">%s</p>
                    %s
                    <p style="margin:28px 0 0;font-size:12px;color:#94a3b8;">Este es un correo automático, no hace falta responderlo.</p>
                  </div>
                </body>
                </html>
                """.formatted(escape(businessName), bodyHtml);
    }

    private static String formatMoney(BigDecimal value, String currency) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "UY"));
        try {
            format.setCurrency(java.util.Currency.getInstance(currency));
        } catch (IllegalArgumentException ignored) {
            // fall back to default currency symbol if the code is unrecognized
        }
        format.setMaximumFractionDigits(0);
        return format.format(value);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
