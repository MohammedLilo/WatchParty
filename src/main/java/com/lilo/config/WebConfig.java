package com.lilo.config;

import com.lilo.service.FileStorageService;
import com.lilo.shared.WebConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This maps 'http://localhost:8081/thumbnails/image.jpg'
        registry.addResourceHandler(WebConstants.thumbnailsUrlPattern)
                .addResourceLocations(FileStorageService.ROOT.toUri().toString())
                .setCachePeriod(180); // Cache images for 1 hour
    }
}