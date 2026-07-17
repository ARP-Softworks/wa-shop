package uy.washop.seo.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.technicalservice.domain.TechnicalService;

@Component
public class JsonLdBuilder {

    private final ObjectMapper objectMapper;
    private final SeoUrlService seoUrlService;
    private final SeoMetaBuilder seoMetaBuilder;

    public JsonLdBuilder(ObjectMapper objectMapper, SeoUrlService seoUrlService, SeoMetaBuilder seoMetaBuilder) {
        this.objectMapper = objectMapper;
        this.seoUrlService = seoUrlService;
        this.seoMetaBuilder = seoMetaBuilder;
    }

    public List<String> organizationAndStore(SiteSettings settings) {
        List<String> scripts = new ArrayList<>();
        if (settings == null) {
            return scripts;
        }
        ObjectNode org = objectMapper.createObjectNode();
        org.put("@context", "https://schema.org");
        org.put("@type", "Organization");
        putIfText(org, "name", settings.getBusinessName());
        putIfText(org, "url", seoUrlService.resolvePublicSiteUrl() + "/");
        putIfText(org, "logo", settings.getLogoUrl());
        putIfText(org, "email", settings.getContactEmail());
        if (StringUtils.hasText(settings.getWhatsappNumber())) {
            org.put("telephone", settings.getWhatsappNumber());
        }
        ObjectNode address = postalAddress(settings);
        if (address != null) {
            org.set("address", address);
        }
        if (StringUtils.hasText(settings.getInstagramUrl())) {
            ArrayNode sameAs = objectMapper.createArrayNode();
            sameAs.add(settings.getInstagramUrl());
            org.set("sameAs", sameAs);
        }
        scripts.add(write(org));

        ObjectNode store = objectMapper.createObjectNode();
        store.put("@context", "https://schema.org");
        store.put("@type", "ElectronicsStore");
        putIfText(store, "name", settings.getBusinessName());
        putIfText(store, "url", seoUrlService.resolvePublicSiteUrl() + "/");
        putIfText(store, "image", firstImage(settings));
        putIfText(store, "email", settings.getContactEmail());
        if (StringUtils.hasText(settings.getWhatsappNumber())) {
            store.put("telephone", settings.getWhatsappNumber());
        }
        putIfText(store, "openingHours", settings.getOpeningHours());
        ObjectNode storeAddress = postalAddress(settings);
        if (storeAddress != null) {
            store.set("address", storeAddress);
        }
        scripts.add(write(store));
        return scripts;
    }

    public String product(Product product, String imageUrl, SiteSettings settings) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("@context", "https://schema.org");
        node.put("@type", "Product");
        putIfText(node, "name", product.getName());
        putIfText(node, "description", seoMetaBuilder.firstNonBlank(product.getMetaDescription(), product.getDescription()));
        putIfText(node, "sku", product.getSlug());
        putIfText(node, "image", imageUrl);
        putIfText(node, "url", seoUrlService.absoluteUrl(seoUrlService.productPath(product)));
        if (StringUtils.hasText(product.getModel())) {
            putIfText(node, "model", product.getModel());
        }
        ObjectNode offer = objectMapper.createObjectNode();
        offer.put("@type", "Offer");
        offer.put("priceCurrency", product.getCurrency() != null ? product.getCurrency().name() : "UYU");
        if (product.getPrice() != null) {
            offer.put("price", product.getPrice().toPlainString());
        }
        offer.put("availability", product.getStock() > 0
                ? "https://schema.org/InStock"
                : "https://schema.org/OutOfStock");
        offer.put("url", seoUrlService.absoluteUrl(seoUrlService.productPath(product)));
        if (settings != null && StringUtils.hasText(settings.getBusinessName())) {
            ObjectNode seller = objectMapper.createObjectNode();
            seller.put("@type", "Organization");
            seller.put("name", settings.getBusinessName());
            offer.set("seller", seller);
        }
        node.set("offers", offer);
        if (product.getCondition() == ProductCondition.USED) {
            node.put("itemCondition", "https://schema.org/UsedCondition");
        } else if (product.getCondition() == ProductCondition.NEW) {
            node.put("itemCondition", "https://schema.org/NewCondition");
        }
        // Never include IMEI or other sensitive identifiers.
        return write(node);
    }

    public String breadcrumb(List<BreadcrumbItem> items) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("@context", "https://schema.org");
        node.put("@type", "BreadcrumbList");
        ArrayNode list = objectMapper.createArrayNode();
        int position = 1;
        for (BreadcrumbItem item : items) {
            ObjectNode element = objectMapper.createObjectNode();
            element.put("@type", "ListItem");
            element.put("position", position++);
            putIfText(element, "name", item.name());
            putIfText(element, "item", seoUrlService.absoluteUrl(item.path()));
            list.add(element);
        }
        node.set("itemListElement", list);
        return write(node);
    }

    public String service(TechnicalService service, SiteSettings settings) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("@context", "https://schema.org");
        node.put("@type", "Service");
        putIfText(node, "name", service.getName());
        putIfText(node, "description", seoMetaBuilder.firstNonBlank(service.getMetaDescription(), service.getDescription()));
        putIfText(node, "url", seoUrlService.absoluteUrl(seoUrlService.technicalServicePath(service.getSlug())));
        if (service.getPrice() != null) {
            ObjectNode offer = objectMapper.createObjectNode();
            offer.put("@type", "Offer");
            offer.put("price", service.getPrice().toPlainString());
            offer.put("priceCurrency", service.getCurrency() != null ? service.getCurrency().name() : "UYU");
            node.set("offers", offer);
        }
        if (settings != null && StringUtils.hasText(settings.getBusinessName())) {
            ObjectNode provider = objectMapper.createObjectNode();
            provider.put("@type", "Organization");
            provider.put("name", settings.getBusinessName());
            node.set("provider", provider);
        }
        return write(node);
    }

    public String faqPage() {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("@context", "https://schema.org");
        node.put("@type", "FAQPage");
        ArrayNode mainEntity = objectMapper.createArrayNode();
        for (FaqContent.FaqItem item : FaqContent.items()) {
            ObjectNode question = objectMapper.createObjectNode();
            question.put("@type", "Question");
            question.put("name", item.question());
            ObjectNode accepted = objectMapper.createObjectNode();
            accepted.put("@type", "Answer");
            accepted.put("text", item.answer());
            question.set("acceptedAnswer", accepted);
            mainEntity.add(question);
        }
        node.set("mainEntity", mainEntity);
        return write(node);
    }

    public record BreadcrumbItem(String name, String path) {
    }

    private ObjectNode postalAddress(SiteSettings settings) {
        boolean hasAny = StringUtils.hasText(settings.getAddress())
                || StringUtils.hasText(settings.getCity())
                || StringUtils.hasText(settings.getRegion())
                || StringUtils.hasText(settings.getPostalCode())
                || StringUtils.hasText(settings.getCountry());
        if (!hasAny) {
            return null;
        }
        ObjectNode address = objectMapper.createObjectNode();
        address.put("@type", "PostalAddress");
        putIfText(address, "streetAddress", settings.getAddress());
        putIfText(address, "addressLocality", settings.getCity());
        putIfText(address, "addressRegion", settings.getRegion());
        putIfText(address, "postalCode", settings.getPostalCode());
        putIfText(address, "addressCountry", settings.getCountry());
        return address;
    }

    private String firstImage(SiteSettings settings) {
        if (StringUtils.hasText(settings.getDefaultSocialImageUrl())) {
            return settings.getDefaultSocialImageUrl();
        }
        return settings.getLogoUrl();
    }

    private void putIfText(ObjectNode node, String field, String value) {
        String sanitized = seoMetaBuilder.sanitizePlainText(value);
        if (StringUtils.hasText(sanitized)) {
            node.put(field, sanitized);
        } else if (StringUtils.hasText(value) && (field.equals("url") || field.equals("logo") || field.equals("image") || field.equals("item"))) {
            node.put(field, value.trim());
        }
    }

    private String write(ObjectNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar JSON-LD", ex);
        }
    }
}
