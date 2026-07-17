package uy.washop.media.infrastructure;

import java.nio.file.Path;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uy.washop.config.AppProperties;

@Configuration
@ConditionalOnProperty(name = "app.media.provider", havingValue = "local")
public class LocalMediaWebConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public LocalMediaWebConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path baseDir = Path.of(appProperties.getMedia().getLocal().getBaseDir()).toAbsolutePath().normalize();
        String location = baseDir.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/media/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
