package com.dronemission.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Solo se activa cuando el frontend compilado está dentro del backend (despliegue en la nube).
 * Sirve los archivos estáticos tal cual (JS, íconos, manifest...) y, para las rutas de React
 * sin extensión (/missions, /drones/3...), devuelve index.html para que React Router muestre
 * la página correcta al recargar. Un archivo inexistente con extensión responde 404.
 */
@Configuration
@ConditionalOnResource(resources = "classpath:/static/index.html")
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        if (!resourcePath.isEmpty()) {
                            Resource requested = location.createRelative(resourcePath);
                            if (requested.exists() && requested.isReadable()) {
                                return requested;
                            }
                        }
                        boolean isAppRoute = !resourcePath.startsWith("api/") && !resourcePath.contains(".");
                        return isAppRoute ? location.createRelative("index.html") : null;
                    }
                });
    }
}
