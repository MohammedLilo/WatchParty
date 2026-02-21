package com.lilo.config;

import com.lilo.service.LocalFileStorageService;
import com.lilo.shared.WebConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonStringProperty;

@Profile("dev")
@Configuration
public class DevWebConfig implements WebMvcConfigurer {


    @Value("${app.web.static-resources-caching-period-in-seconds}")
    private int staticResourcesCachingPeriodInSeconds;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This maps 'http://localhost:8081/thumbnails/image.jpg'
//        registry.addResourceHandler(WebConstants.thumbnailsUrlPattern)
//                .addResourceLocations(LocalFileStorageService.ROOT.toUri().toString())
//                .setCachePeriod(180); // Cache images for 3 mins
//
//        registry.addResourceHandler(WebConstants.videosUrlPattern)
//                .addResourceLocations(LocalFileStorageService.ROOT.toUri().toString())
//                .setCachePeriod(3600);
//
//        registry.addResourceHandler(WebConstants.profilePictureUrlPattern)
//                .addResourceLocations(LocalFileStorageService.ROOT.toUri().toString())
//                .setCachePeriod(3600);
                registry.addResourceHandler(WebConstants.staticResourcesUrlPattern)
                .addResourceLocations(LocalFileStorageService.ROOT.toUri().toString())
                .setCachePeriod(staticResourcesCachingPeriodInSeconds);
    }
}