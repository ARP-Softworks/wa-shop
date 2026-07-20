package uy.washop.admin;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uy.washop.auth.domain.User;
import uy.washop.auth.domain.UserRole;
import uy.washop.auth.infrastructure.UserRepository;
import uy.washop.inquiry.domain.Inquiry;
import uy.washop.inquiry.domain.InquirySource;
import uy.washop.inquiry.domain.InquiryStatus;
import uy.washop.inquiry.infrastructure.InquiryRepository;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.security.AdminUserDetails;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.settings.infrastructure.SiteSettingsRepository;
import uy.washop.shared.domain.CurrencyCode;
import uy.washop.technicalservice.domain.TechnicalService;
import uy.washop.technicalservice.infrastructure.TechnicalServiceRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminPanelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private SiteSettingsRepository siteSettingsRepository;

    @Autowired
    private TechnicalServiceRepository technicalServiceRepository;

    private AdminUserDetails adminDetails;
    private UUID productId;

    @BeforeEach
    void setUp() {
        inquiryRepository.deleteAll();
        productRepository.deleteAll();
        technicalServiceRepository.deleteAll();
        siteSettingsRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setEmail("admin@washop.uy");
        admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
        admin.setFirstName("Admin");
        admin.setLastName("Test");
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        admin = userRepository.save(admin);
        adminDetails = new AdminUserDetails(admin);

        Product product = new Product();
        product.setSlug("iphone-admin-test");
        product.setName("iPhone Admin");
        product.setProductType(ProductType.IPHONE);
        product.setCondition(ProductCondition.USED);
        product.setStorageCapacity("128GB");
        product.setColor("Black");
        product.setBatteryHealth(88);
        product.setPrice(new BigDecimal("25000.00"));
        product.setCurrency(CurrencyCode.UYU);
        product.setStock(2);
        product.setImei("356938035643900");
        product.setPublished(true);
        product.setFeatured(false);
        productId = productRepository.save(product).getId();

        Inquiry inquiry = new Inquiry();
        inquiry.setCustomerName("Cliente Demo");
        inquiry.setPhone("099123456");
        inquiry.setMessage("Consulta de prueba");
        inquiry.setStatus(InquiryStatus.NEW);
        inquiry.setSource(InquirySource.WHATSAPP_CLICK);
        inquiry.setProduct(product);
        inquiryRepository.save(inquiry);

        SiteSettings settings = new SiteSettings();
        settings.setId(SiteSettings.DEFAULT_ID);
        settings.setBusinessName("WA Shop");
        settings.setWhatsappNumber("59899111222");
        settings.setContactEmail("hola@washop.uy");
        siteSettingsRepository.save(settings);

        TechnicalService service = new TechnicalService();
        service.setName("Cambio de pantalla");
        service.setSlug("cambio-pantalla");
        service.setPrice(new BigDecimal("4500.00"));
        service.setCurrency(CurrencyCode.UYU);
        service.setActive(true);
        technicalServiceRepository.save(service);
    }

    @Test
    void dashboardReturnsRealCounts() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard").with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedProducts", is(1)))
                .andExpect(jsonPath("$.usedDevices", is(1)))
                .andExpect(jsonPath("$.pendingInquiries", is(1)))
                .andExpect(jsonPath("$.activeTechnicalServices", is(1)))
                .andExpect(jsonPath("$.recentProducts", hasSize(1)))
                .andExpect(jsonPath("$.recentInquiries", hasSize(1)));
    }

    @Test
    void productCrudAndPublish() throws Exception {
        String body = """
                {
                  "name": "iPhone 15 Pro",
                  "slug": "iphone-15-pro-admin",
                  "model": "iPhone 15 Pro",
                  "description": "Equipo nuevo",
                  "productType": "IPHONE",
                  "promoBuyQuantity": null,
                  "promoPayQuantity": null,
                  "published": false,
                  "featured": true,
                  "categoryId": null,
                  "features": [{"name": "Chip", "value": "A17"}],
                  "compatibleModels": [],
                  "variants": [{
                    "condition": "NEW",
                    "storageCapacity": "256GB",
                    "color": "Titanio",
                    "batteryHealth": null,
                    "price": 45990,
                    "previousPrice": null,
                    "currency": "UYU",
                    "stock": 3,
                    "warranty": "1 año",
                    "imei": null,
                    "published": true,
                    "images": [{"url": "https://cdn.example/main.jpg", "publicId": null, "altText": "Principal", "position": 0, "mainImage": true}]
                  }]
                }
                """;

        String id = com.jayway.jsonpath.JsonPath.read(
                mockMvc.perform(post("/api/admin/products")
                                .with(user(adminDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.slug").value("iphone-15-pro-admin"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.id"
        );
        mockMvc.perform(patch("/api/admin/products/{id}/publish", id)
                        .with(user(adminDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"published\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));

        mockMvc.perform(get("/api/admin/products").with(user(adminDetails)).param("q", "15 Pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(delete("/api/admin/products/{id}", id)
                        .with(user(adminDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void usedIphoneRequiresBatteryHealth() throws Exception {
        String body = """
                {
                  "name": "Usado sin batería",
                  "slug": "usado-sin-bateria",
                  "productType": "IPHONE",
                  "published": false,
                  "featured": false,
                  "variants": [{
                    "condition": "USED",
                    "price": 10000,
                    "currency": "UYU",
                    "stock": 1,
                    "published": false,
                    "images": []
                  }]
                }
                """;

        mockMvc.perform(post("/api/admin/products")
                        .with(user(adminDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void inquiryStatusHistoryAndSettings() throws Exception {
        UUID inquiryId = inquiryRepository.findAll().getFirst().getId();

        mockMvc.perform(put("/api/admin/inquiries/{id}/status", inquiryId)
                        .with(user(adminDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"IN_PROGRESS","note":"Contactado","adminNotes":"Seguir mañana"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.adminNotes").value("Seguir mañana"))
                .andExpect(jsonPath("$.statusHistory", hasSize(1)));

        mockMvc.perform(put("/api/admin/settings")
                        .with(user(adminDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessName": "WA Shop Uruguay",
                                  "whatsappNumber": "59899111222",
                                  "instagramUrl": "https://www.instagram.com/wa.shop.uy/",
                                  "address": "Montevideo",
                                  "openingHours": "Lun-Vie 10-19",
                                  "contactEmail": "hola@washop.uy",
                                  "logoUrl": "https://cdn.example/logo.png",
                                  "logoPublicId": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("WA Shop Uruguay"));

        mockMvc.perform(get("/api/admin/audit-logs").with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void categoryAndTechnicalServiceCrud() throws Exception {
        String categoryId = com.jayway.jsonpath.JsonPath.read(
                mockMvc.perform(post("/api/admin/categories")
                                .with(user(adminDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"iPhones","slug":"iphones","description":"Equipos","active":true}
                                        """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.slug").value("iphones"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.id"
        );
        mockMvc.perform(delete("/api/admin/categories/{id}", categoryId)
                        .with(user(adminDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/admin/technical-services")
                        .with(user(adminDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Limpieza",
                                  "slug":"limpieza",
                                  "description":"Limpieza interna",
                                  "price":1500,
                                  "currency":"UYU",
                                  "estimatedTime":"1 hora",
                                  "active":true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Limpieza"));
    }

    @Test
    void adminEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/products/{id}", productId)).andExpect(status().isUnauthorized());
    }
}
