package uy.washop.product.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.product.domain.ProductVariant;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.product.infrastructure.ProductVariantRepository;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.settings.infrastructure.SiteSettingsRepository;
import uy.washop.shared.domain.CurrencyCode;
import uy.washop.technicalservice.domain.TechnicalService;
import uy.washop.technicalservice.infrastructure.TechnicalServiceRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicCatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private SiteSettingsRepository siteSettingsRepository;

    @Autowired
    private TechnicalServiceRepository technicalServiceRepository;

    private String usedSlug;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        technicalServiceRepository.deleteAll();
        siteSettingsRepository.deleteAll();

        Product published = new Product();
        published.setSlug("iphone-14-public");
        published.setName("iPhone 14 Público");
        published.setModel("iPhone 14");
        published.setProductType(ProductType.IPHONE);
        published.setCondition(ProductCondition.USED);
        published.setStorageCapacity("128GB");
        published.setColor("Midnight");
        published.setBatteryHealth(91);
        published.setPrice(new BigDecimal("28990.00"));
        published.setPreviousPrice(new BigDecimal("31990.00"));
        published.setCurrency(CurrencyCode.UYU);
        published.setStock(1);
        published.setWarranty("3 meses");
        published.setImei("356938035643811");
        published.setPublished(true);
        published.setFeatured(true);
        published = productRepository.save(published);
        usedSlug = published.getSlug();

        ProductVariant publishedVariant = new ProductVariant();
        publishedVariant.setProduct(published);
        publishedVariant.setCondition(ProductCondition.USED);
        publishedVariant.setStorageCapacity("128GB");
        publishedVariant.setColor("Midnight");
        publishedVariant.setBatteryHealth(91);
        publishedVariant.setPrice(new BigDecimal("28990.00"));
        publishedVariant.setPreviousPrice(new BigDecimal("31990.00"));
        publishedVariant.setCurrency(CurrencyCode.UYU);
        publishedVariant.setStock(1);
        publishedVariant.setWarranty("3 meses");
        publishedVariant.setImei("356938035643811");
        publishedVariant.setPublished(true);
        productVariantRepository.save(publishedVariant);

        Product unpublished = new Product();
        unpublished.setSlug("iphone-oculto");
        unpublished.setName("Oculto");
        unpublished.setProductType(ProductType.IPHONE);
        unpublished.setCondition(ProductCondition.NEW);
        unpublished.setPrice(new BigDecimal("1000.00"));
        unpublished.setCurrency(CurrencyCode.UYU);
        unpublished.setStock(1);
        unpublished.setPublished(false);
        unpublished.setFeatured(false);
        productRepository.save(unpublished);

        SiteSettings settings = new SiteSettings();
        settings.setId(SiteSettings.DEFAULT_ID);
        settings.setBusinessName("WA Shop");
        settings.setWhatsappNumber("59890000000");
        settings.setContactEmail("hola@washop.uy");
        siteSettingsRepository.save(settings);

        TechnicalService service = new TechnicalService();
        service.setName("Cambio de batería");
        service.setSlug("cambio-bateria");
        service.setDescription("Servicio de batería");
        service.setPrice(new BigDecimal("3200.00"));
        service.setCurrency(CurrencyCode.UYU);
        service.setEstimatedTime("24 hs");
        service.setActive(true);
        technicalServiceRepository.save(service);
    }

    @Test
    void searchReturnsOnlyPublishedAndSupportsFilters() throws Exception {
        mockMvc.perform(get("/api/public/products")
                        .param("productType", "IPHONE")
                        .param("condition", "USED")
                        .param("model", "iPhone 14")
                        .param("minBatteryHealth", "90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].slug").value("iphone-14-public"))
                .andExpect(jsonPath("$.content[0].imei").doesNotExist());
    }

    @Test
    void detailBySlugExcludesImei() throws Exception {
        mockMvc.perform(get("/api/public/products/{slug}", usedSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 14 Público"))
                .andExpect(jsonPath("$.imei").doesNotExist())
                .andExpect(jsonPath("$.batteryHealth").value(91));
    }

    @Test
    void unpublishedProductIsNotFound() throws Exception {
        mockMvc.perform(get("/api/public/products/iphone-oculto"))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicSettingsAndServicesAreAvailable() throws Exception {
        mockMvc.perform(get("/api/public/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsappNumber").value("59890000000"))
                .andExpect(jsonPath("$.businessName").value("WA Shop"));

        mockMvc.perform(get("/api/public/technical-services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("cambio-bateria"));
    }
}
