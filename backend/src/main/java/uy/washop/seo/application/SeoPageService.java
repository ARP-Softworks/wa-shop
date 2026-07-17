package uy.washop.seo.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uy.washop.category.domain.Category;
import uy.washop.category.infrastructure.CategoryRepository;
import uy.washop.config.AppProperties;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductImage;
import uy.washop.product.domain.ProductType;
import uy.washop.product.infrastructure.ProductImageRepository;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.seo.api.dto.PublicSeoConfigResponse;
import uy.washop.seo.api.dto.SeoPageMetaResponse;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.settings.infrastructure.SiteSettingsRepository;
import uy.washop.technicalservice.domain.TechnicalService;
import uy.washop.technicalservice.infrastructure.TechnicalServiceRepository;

@Service
public class SeoPageService {

    private static final Set<String> IPHONE_RESERVED = Set.of("nuevos", "usados", "modelo");

    private final SiteSettingsRepository siteSettingsRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final TechnicalServiceRepository technicalServiceRepository;
    private final SeoUrlService seoUrlService;
    private final SeoMetaBuilder seoMetaBuilder;
    private final JsonLdBuilder jsonLdBuilder;
    private final AppProperties appProperties;

    public SeoPageService(
            SiteSettingsRepository siteSettingsRepository,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            CategoryRepository categoryRepository,
            TechnicalServiceRepository technicalServiceRepository,
            SeoUrlService seoUrlService,
            SeoMetaBuilder seoMetaBuilder,
            JsonLdBuilder jsonLdBuilder,
            AppProperties appProperties
    ) {
        this.siteSettingsRepository = siteSettingsRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.technicalServiceRepository = technicalServiceRepository;
        this.seoUrlService = seoUrlService;
        this.seoMetaBuilder = seoMetaBuilder;
        this.jsonLdBuilder = jsonLdBuilder;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public PublicSeoConfigResponse publicConfig() {
        SiteSettings settings = loadSettings();
        return new PublicSeoConfigResponse(
                seoUrlService.resolvePublicSiteUrl(),
                seoMetaBuilder.firstNonBlank(settings != null ? settings.getDefaultSeoTitle() : null, "WA Shop"),
                seoMetaBuilder.firstNonBlank(
                        settings != null ? settings.getDefaultMetaDescription() : null,
                        "iPhone nuevos y usados, accesorios y servicio técnico en Uruguay."
                ),
                settings != null ? settings.getDefaultSocialImageUrl() : null,
                settings != null ? settings.getBusinessName() : "WA Shop",
                settings != null ? settings.getLogoUrl() : null,
                settings != null ? settings.getWhatsappNumber() : null,
                settings != null ? settings.getAddress() : null,
                settings != null ? settings.getOpeningHours() : null,
                settings != null ? settings.getContactEmail() : null,
                settings != null ? settings.getInstagramUrl() : null,
                settings != null ? settings.getCountry() : null,
                settings != null ? settings.getRegion() : null,
                settings != null ? settings.getCity() : null,
                settings != null ? settings.getPostalCode() : null,
                resolveGoogleVerification(settings),
                resolveBingVerification(settings)
        );
    }

    @Transactional(readOnly = true)
    public SeoPageMetaResponse resolvePage(String rawPath) {
        String path = SeoUrlService.normalizePath(rawPath);
        SiteSettings settings = loadSettings();
        String brand = settings != null && StringUtils.hasText(settings.getBusinessName())
                ? settings.getBusinessName()
                : "WA Shop";
        String defaultTitle = seoMetaBuilder.firstNonBlank(
                settings != null ? settings.getDefaultSeoTitle() : null,
                brand + " | iPhone Uruguay"
        );
        String defaultDescription = seoMetaBuilder.firstNonBlank(
                settings != null ? settings.getDefaultMetaDescription() : null,
                "iPhone nuevos y usados, accesorios y servicio técnico en Uruguay."
        );
        String defaultImage = settings != null
                ? seoMetaBuilder.firstNonBlank(settings.getDefaultSocialImageUrl(), settings.getLogoUrl())
                : null;

        if (path.startsWith("/admin") || path.equals("/login") || path.startsWith("/login/")) {
            return page(defaultTitle, defaultDescription, path, false, true, defaultImage, brand, List.of(), false);
        }

        return switch (path) {
            case "/" -> home(settings, brand, defaultTitle, defaultDescription, defaultImage);
            case "/iphone" -> listing(
                    "iPhone en Uruguay | " + brand,
                    "Catálogo de iPhone nuevos y usados en WA Shop. Consultá por WhatsApp.",
                    path,
                    defaultImage,
                    brand,
                    settings,
                    List.of(
                            new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                            new JsonLdBuilder.BreadcrumbItem("iPhone", "/iphone")
                    )
            );
            case "/iphone/nuevos" -> listing(
                    "iPhone nuevos | " + brand,
                    "iPhone nuevos disponibles en WA Shop Uruguay.",
                    path,
                    defaultImage,
                    brand,
                    settings,
                    List.of(
                            new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                            new JsonLdBuilder.BreadcrumbItem("iPhone", "/iphone"),
                            new JsonLdBuilder.BreadcrumbItem("Nuevos", "/iphone/nuevos")
                    )
            );
            case "/iphone/usados" -> listing(
                    "iPhone usados | " + brand,
                    "iPhone usados seleccionados en WA Shop Uruguay.",
                    path,
                    defaultImage,
                    brand,
                    settings,
                    List.of(
                            new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                            new JsonLdBuilder.BreadcrumbItem("iPhone", "/iphone"),
                            new JsonLdBuilder.BreadcrumbItem("Usados", "/iphone/usados")
                    )
            );
            case "/accesorios" -> listing(
                    "Accesorios para iPhone | " + brand,
                    "Accesorios para iPhone en WA Shop Uruguay.",
                    path,
                    defaultImage,
                    brand,
                    settings,
                    List.of(
                            new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                            new JsonLdBuilder.BreadcrumbItem("Accesorios", "/accesorios")
                    )
            );
            case "/servicio-tecnico" -> listing(
                    "Servicio técnico de iPhone | " + brand,
                    "Servicio técnico de iPhone en WA Shop. Consultá tiempos y precios.",
                    path,
                    defaultImage,
                    brand,
                    settings,
                    List.of(
                            new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                            new JsonLdBuilder.BreadcrumbItem("Servicio técnico", "/servicio-tecnico")
                    )
            );
            case "/contacto" -> listing(
                    "Contacto | " + brand,
                    "Contactá a WA Shop por WhatsApp, email o visitanos.",
                    path,
                    defaultImage,
                    brand,
                    settings,
                    List.of(
                            new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                            new JsonLdBuilder.BreadcrumbItem("Contacto", "/contacto")
                    )
            );
            case "/preguntas-frecuentes" -> faq(settings, brand, defaultImage);
            default -> resolveDynamic(path, settings, brand, defaultTitle, defaultDescription, defaultImage);
        };
    }

    private SeoPageMetaResponse home(
            SiteSettings settings,
            String brand,
            String defaultTitle,
            String defaultDescription,
            String defaultImage
    ) {
        List<String> jsonLd = new ArrayList<>(jsonLdBuilder.organizationAndStore(settings));
        return page(defaultTitle, defaultDescription, "/", true, false, defaultImage, brand, jsonLd, false);
    }

    private SeoPageMetaResponse faq(SiteSettings settings, String brand, String defaultImage) {
        List<String> jsonLd = new ArrayList<>(jsonLdBuilder.organizationAndStore(settings));
        jsonLd.add(jsonLdBuilder.faqPage());
        jsonLd.add(jsonLdBuilder.breadcrumb(List.of(
                new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                new JsonLdBuilder.BreadcrumbItem("Preguntas frecuentes", "/preguntas-frecuentes")
        )));
        return page(
                "Preguntas frecuentes | " + brand,
                "Respuestas sobre compra de iPhone, garantía, servicio técnico y contacto en WA Shop Uruguay.",
                "/preguntas-frecuentes",
                true,
                false,
                defaultImage,
                brand,
                jsonLd,
                false
        );
    }

    private SeoPageMetaResponse listing(
            String title,
            String description,
            String path,
            String image,
            String brand,
            SiteSettings settings,
            List<JsonLdBuilder.BreadcrumbItem> crumbs
    ) {
        List<String> jsonLd = new ArrayList<>(jsonLdBuilder.organizationAndStore(settings));
        jsonLd.add(jsonLdBuilder.breadcrumb(crumbs));
        return page(title, description, path, true, false, image, brand, jsonLd, false);
    }

    private SeoPageMetaResponse resolveDynamic(
            String path,
            SiteSettings settings,
            String brand,
            String defaultTitle,
            String defaultDescription,
            String defaultImage
    ) {
        if (path.startsWith("/iphone/modelo/")) {
            String model = path.substring("/iphone/modelo/".length());
            return listing(
                    "iPhone " + model + " | " + brand,
                    "Equipos iPhone modelo " + model + " en WA Shop Uruguay.",
                    path,
                    defaultImage,
                    brand,
                    settings,
                    List.of(
                            new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                            new JsonLdBuilder.BreadcrumbItem("iPhone", "/iphone"),
                            new JsonLdBuilder.BreadcrumbItem(model, path)
                    )
            );
        }

        if (path.startsWith("/iphone/") && path.length() > "/iphone/".length()) {
            String slug = path.substring("/iphone/".length());
            if (!slug.contains("/") && !IPHONE_RESERVED.contains(slug.toLowerCase(Locale.ROOT))) {
                return productPage(slug, ProductType.IPHONE, settings, brand, defaultImage);
            }
        }

        if (path.startsWith("/accesorios/categoria/")) {
            String slug = path.substring("/accesorios/categoria/".length());
            if (!slug.contains("/")) {
                return categoryPage(slug, settings, brand, defaultImage);
            }
        }

        if (path.startsWith("/accesorios/") && path.length() > "/accesorios/".length()
                && !path.startsWith("/accesorios/categoria/")) {
            String slug = path.substring("/accesorios/".length());
            if (!slug.contains("/")) {
                return productPage(slug, ProductType.ACCESSORY, settings, brand, defaultImage);
            }
        }

        if (path.startsWith("/servicio-tecnico/") && path.length() > "/servicio-tecnico/".length()) {
            String slug = path.substring("/servicio-tecnico/".length());
            if (!slug.contains("/")) {
                return servicePage(slug, settings, brand, defaultImage);
            }
        }

        return page(defaultTitle, defaultDescription, path, true, false, defaultImage, brand, List.of(), false);
    }

    private SeoPageMetaResponse productPage(
            String slug,
            ProductType expectedType,
            SiteSettings settings,
            String brand,
            String defaultImage
    ) {
        Product product = productRepository.findBySlug(slug).orElse(null);
        if (product == null || !product.isPublished() || product.getProductType() != expectedType) {
            String path = expectedType == ProductType.ACCESSORY ? "/accesorios/" + slug : "/iphone/" + slug;
            return page(
                    "Producto no encontrado | " + brand,
                    "El producto solicitado no está disponible.",
                    path,
                    false,
                    true,
                    defaultImage,
                    brand,
                    List.of(),
                    true
            );
        }

        String path = seoUrlService.productPath(product);
        String title = seoMetaBuilder.firstNonBlank(product.getSeoTitle(), product.getName() + " | " + brand);
        String description = seoMetaBuilder.firstNonBlank(
                product.getMetaDescription(),
                product.getDescription(),
                product.getName() + " en WA Shop Uruguay."
        );
        String image = primaryImageUrl(product);
        if (!StringUtils.hasText(image)) {
            image = defaultImage;
        }
        List<String> jsonLd = new ArrayList<>();
        if (product.isIndexable()) {
            jsonLd.addAll(jsonLdBuilder.organizationAndStore(settings));
            jsonLd.add(jsonLdBuilder.product(product, image, settings));
            List<JsonLdBuilder.BreadcrumbItem> crumbs = new ArrayList<>();
            crumbs.add(new JsonLdBuilder.BreadcrumbItem("Inicio", "/"));
            if (expectedType == ProductType.ACCESSORY) {
                crumbs.add(new JsonLdBuilder.BreadcrumbItem("Accesorios", "/accesorios"));
            } else {
                crumbs.add(new JsonLdBuilder.BreadcrumbItem("iPhone", "/iphone"));
                if (product.getCondition() == ProductCondition.NEW) {
                    crumbs.add(new JsonLdBuilder.BreadcrumbItem("Nuevos", "/iphone/nuevos"));
                } else if (product.getCondition() == ProductCondition.USED) {
                    crumbs.add(new JsonLdBuilder.BreadcrumbItem("Usados", "/iphone/usados"));
                }
            }
            crumbs.add(new JsonLdBuilder.BreadcrumbItem(product.getName(), path));
            jsonLd.add(jsonLdBuilder.breadcrumb(crumbs));
        }
        return page(title, description, path, product.isIndexable(), false, image, product.getName(), jsonLd, false);
    }

    private SeoPageMetaResponse categoryPage(String slug, SiteSettings settings, String brand, String defaultImage) {
        Category category = categoryRepository.findBySlug(slug).orElse(null);
        String path = seoUrlService.categoryPath(slug);
        if (category == null || !category.isActive()) {
            return page(
                    "Categoría no encontrada | " + brand,
                    "La categoría solicitada no está disponible.",
                    path,
                    false,
                    true,
                    defaultImage,
                    brand,
                    List.of(),
                    true
            );
        }
        String title = seoMetaBuilder.firstNonBlank(category.getSeoTitle(), category.getName() + " | " + brand);
        String description = seoMetaBuilder.firstNonBlank(
                category.getMetaDescription(),
                category.getDescription(),
                "Accesorios de la categoría " + category.getName() + " en WA Shop."
        );
        List<String> jsonLd = new ArrayList<>(jsonLdBuilder.organizationAndStore(settings));
        jsonLd.add(jsonLdBuilder.breadcrumb(List.of(
                new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                new JsonLdBuilder.BreadcrumbItem("Accesorios", "/accesorios"),
                new JsonLdBuilder.BreadcrumbItem(category.getName(), path)
        )));
        return page(title, description, path, category.isIndexable(), false, defaultImage, category.getName(), jsonLd, false);
    }

    private SeoPageMetaResponse servicePage(String slug, SiteSettings settings, String brand, String defaultImage) {
        TechnicalService service = technicalServiceRepository.findBySlug(slug).orElse(null);
        String path = seoUrlService.technicalServicePath(slug);
        if (service == null || !service.isActive()) {
            return page(
                    "Servicio no encontrado | " + brand,
                    "El servicio técnico solicitado no está disponible.",
                    path,
                    false,
                    true,
                    defaultImage,
                    brand,
                    List.of(),
                    true
            );
        }
        String title = seoMetaBuilder.firstNonBlank(service.getSeoTitle(), service.getName() + " | " + brand);
        String description = seoMetaBuilder.firstNonBlank(
                service.getMetaDescription(),
                service.getDescription(),
                service.getName() + " en WA Shop Uruguay."
        );
        List<String> jsonLd = new ArrayList<>();
        if (service.isIndexable()) {
            jsonLd.addAll(jsonLdBuilder.organizationAndStore(settings));
            jsonLd.add(jsonLdBuilder.service(service, settings));
            jsonLd.add(jsonLdBuilder.breadcrumb(List.of(
                    new JsonLdBuilder.BreadcrumbItem("Inicio", "/"),
                    new JsonLdBuilder.BreadcrumbItem("Servicio técnico", "/servicio-tecnico"),
                    new JsonLdBuilder.BreadcrumbItem(service.getName(), path)
            )));
        }
        return page(title, description, path, service.isIndexable(), false, defaultImage, service.getName(), jsonLd, false);
    }

    private SeoPageMetaResponse page(
            String title,
            String description,
            String path,
            boolean indexable,
            boolean forceNoindex,
            String ogImage,
            String ogImageAlt,
            List<String> jsonLd,
            boolean notFound
    ) {
        return new SeoPageMetaResponse(
                seoMetaBuilder.truncate(title, 70),
                seoMetaBuilder.truncate(description, 320),
                seoUrlService.absoluteUrl(path),
                seoMetaBuilder.robotsDirective(indexable, forceNoindex || notFound),
                ogImage,
                seoMetaBuilder.sanitizePlainText(ogImageAlt),
                jsonLd == null ? List.of() : List.copyOf(jsonLd),
                notFound
        );
    }

    private String primaryImageUrl(Product product) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByPositionAsc(product.getId());
        return images.stream()
                .filter(ProductImage::isMainImage)
                .map(ProductImage::getUrl)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.getFirst().getUrl());
    }

    private SiteSettings loadSettings() {
        return siteSettingsRepository.findById(SiteSettings.DEFAULT_ID)
                .or(() -> siteSettingsRepository.findAll().stream().findFirst())
                .orElse(null);
    }

    private String resolveGoogleVerification(SiteSettings settings) {
        if (settings != null && StringUtils.hasText(settings.getGoogleSiteVerification())) {
            return settings.getGoogleSiteVerification().trim();
        }
        return blankToNull(appProperties.getSeo().getGoogleSiteVerification());
    }

    private String resolveBingVerification(SiteSettings settings) {
        if (settings != null && StringUtils.hasText(settings.getBingSiteVerification())) {
            return settings.getBingSiteVerification().trim();
        }
        return blankToNull(appProperties.getSeo().getBingSiteVerification());
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
