package uy.washop.seo.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uy.washop.product.domain.Product;
import uy.washop.product.domain.ProductCondition;
import uy.washop.product.domain.ProductType;
import uy.washop.product.infrastructure.ProductRepository;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.settings.infrastructure.SiteSettingsRepository;
import uy.washop.shared.domain.CurrencyCode;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SitemapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SiteSettingsRepository siteSettingsRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        siteSettingsRepository.deleteAll();

        SiteSettings settings = new SiteSettings();
        settings.setId(SiteSettings.DEFAULT_ID);
        settings.setBusinessName("WA Shop");
        settings.setPublicSiteUrl("https://washop.uy");
        siteSettingsRepository.save(settings);

        Product published = new Product();
        published.setSlug("iphone-14-seo");
        published.setName("iPhone 14 SEO");
        published.setProductType(ProductType.IPHONE);
        published.setCondition(ProductCondition.USED);
        published.setPrice(new BigDecimal("28990.00"));
        published.setCurrency(CurrencyCode.UYU);
        published.setStock(1);
        published.setBatteryHealth(90);
        published.setPublished(true);
        published.setFeatured(false);
        published.setIndexable(true);
        productRepository.save(published);

        Product unpublished = new Product();
        unpublished.setSlug("iphone-oculto-seo");
        unpublished.setName("Oculto SEO");
        unpublished.setProductType(ProductType.IPHONE);
        unpublished.setCondition(ProductCondition.NEW);
        unpublished.setPrice(new BigDecimal("1000.00"));
        unpublished.setCurrency(CurrencyCode.UYU);
        unpublished.setStock(1);
        unpublished.setPublished(false);
        unpublished.setFeatured(false);
        unpublished.setIndexable(true);
        productRepository.save(unpublished);

        Product noindex = new Product();
        noindex.setSlug("iphone-noindex");
        noindex.setName("No Index");
        noindex.setProductType(ProductType.IPHONE);
        noindex.setCondition(ProductCondition.NEW);
        noindex.setPrice(new BigDecimal("2000.00"));
        noindex.setCurrency(CurrencyCode.UYU);
        noindex.setStock(1);
        noindex.setPublished(true);
        noindex.setFeatured(false);
        noindex.setIndexable(false);
        productRepository.save(noindex);
    }

    @Test
    void sitemapIncludesPublishedIndexableProductsWithAbsoluteLocs() throws Exception {
        mockMvc.perform(get("/sitemap.xml").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().string(containsString("https://washop.uy/iphone/iphone-14-seo")))
                .andExpect(content().string(containsString("https://washop.uy/iphone/nuevos")))
                .andExpect(content().string(not(containsString("iphone-oculto-seo"))))
                .andExpect(content().string(not(containsString("iphone-noindex"))));
    }
}
