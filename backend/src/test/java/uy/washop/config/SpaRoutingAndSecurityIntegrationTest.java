package uy.washop.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SpaRoutingAndSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void spaFallbackServesIndexForAngularRoutes() throws Exception {
        mockMvc.perform(get("/iphone"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("wa-shop-spa-fallback")));

        mockMvc.perform(get("/admin/productos"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("wa-shop-spa-fallback")));
    }

    @Test
    void unknownProductSlugReturnsNotFoundDocument() throws Exception {
        mockMvc.perform(get("/iphone/producto-inexistente-seo"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("wa-shop-spa-fallback")));
    }

    @Test
    void legacyCatalogPathsRedirectPermanently() throws Exception {
        mockMvc.perform(get("/catalogo"))
                .andExpect(status().isMovedPermanently());
        mockMvc.perform(get("/catalogo/iphone-demo"))
                .andExpect(status().isMovedPermanently());
    }

    @Test
    void healthEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/api/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void adminApiIsNotPublic() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/settings")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/media")).andExpect(status().isUnauthorized());
    }

    @Test
    void apiPathsAreNotCapturedBySpaFallback() throws Exception {
        mockMvc.perform(get("/api/public/settings"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("wa-shop-spa-fallback"))));
    }
}
