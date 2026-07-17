package uy.washop.seo.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uy.washop.settings.domain.SiteSettings;
import uy.washop.settings.infrastructure.SiteSettingsRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RobotsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SiteSettingsRepository siteSettingsRepository;

    @BeforeEach
    void setUp() {
        siteSettingsRepository.deleteAll();
        SiteSettings settings = new SiteSettings();
        settings.setId(SiteSettings.DEFAULT_ID);
        settings.setBusinessName("WA Shop");
        settings.setPublicSiteUrl("https://washop.uy");
        siteSettingsRepository.save(settings);
    }

    @Test
    void robotsTxtDisallowsAdminAndPointsToSitemap() throws Exception {
        mockMvc.perform(get("/robots.txt").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(containsString("Disallow: /admin")))
                .andExpect(content().string(containsString("Disallow: /api/")))
                .andExpect(content().string(containsString("Sitemap: https://washop.uy/sitemap.xml")));
    }
}
