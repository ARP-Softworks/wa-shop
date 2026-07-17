package uy.washop.config;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serves the Angular production build from {@code classpath:/static/} and
 * forwards unknown non-API paths to {@code index.html} so browser reloads
 * work with the Angular router. Controllers under {@code /api/**} keep precedence.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.setOrder(Integer.MAX_VALUE);
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        if (isApiOrActuatorPath(resourcePath)) {
                            return null;
                        }
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        Resource index = new ClassPathResource("/static/index.html");
                        return index.exists() && index.isReadable() ? index : null;
                    }
                });
    }

    private static boolean isApiOrActuatorPath(String resourcePath) {
        return resourcePath.equals("api")
                || resourcePath.startsWith("api/")
                || resourcePath.equals("actuator")
                || resourcePath.startsWith("actuator/")
                || resourcePath.equals("robots.txt")
                || resourcePath.equals("sitemap.xml");
    }
}
