package uy.washop.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uy.washop.auth.domain.User;
import uy.washop.auth.domain.UserRole;
import uy.washop.auth.infrastructure.UserRepository;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.product.domain.ProductVariant;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.product.infrastructure.ProductVariantRepository;
import uy.washop.shared.domain.CurrencyCode;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private UUID productId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        productVariantRepository.deleteAll();
        productRepository.deleteAll();

        User admin = new User();
        admin.setEmail("admin@washop.uy");
        admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
        admin.setFirstName("Admin");
        admin.setLastName("Test");
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        Product product = new Product();
        product.setSlug("iphone-auth-test-" + UUID.randomUUID());
        product.setName("iPhone Auth Test");
        product.setProductType(ProductType.IPHONE);
        product.setCondition(ProductCondition.USED);
        product.setPrice(new BigDecimal("20000.00"));
        product.setCurrency(CurrencyCode.UYU);
        product.setStock(1);
        product.setBatteryHealth(90);
        product.setPublished(true);
        product.setFeatured(false);
        product = productRepository.save(product);
        productId = product.getId();

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setCondition(ProductCondition.USED);
        variant.setPrice(new BigDecimal("20000.00"));
        variant.setCurrency(CurrencyCode.UYU);
        variant.setStock(1);
        variant.setImei("356938035643899");
        variant.setBatteryHealth(90);
        variant.setPublished(true);
        productVariantRepository.save(variant);
    }

    @Test
    void loginSucceedsWithValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@washop.uy","password":"ChangeMe123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@washop.uy"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(request().sessionAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        notNullValue()
                ));
    }

    @Test
    void loginFailsWithGenericMessageForInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@washop.uy","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"missing@washop.uy","password":"ChangeMe123!"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void publicEndpointIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone Auth Test"));
    }

    @Test
    void adminEndpointIsBlockedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/products/{id}", productId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointIsAccessibleForAuthenticatedAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/products/{id}", productId)
                        .with(user("admin@washop.uy").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variants[0].imei").value("356938035643899"));
    }

    @Test
    void logoutClearsSession() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@washop.uy","password":"ChangeMe123!"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicProductResponseNeverIncludesImei() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/public/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imei").doesNotExist())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain("356938035643899");
        assertThat(result.getResponse().getContentAsString()).doesNotContain("\"imei\"");
    }
}
