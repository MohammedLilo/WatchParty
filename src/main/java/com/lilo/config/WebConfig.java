package com.lilo.config;

import com.lilo.service.FileStorageService;
import com.lilo.shared.WebConstants;
import org.apache.tika.mime.MimeTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.BodyFilters;

import java.util.Collections;

import static java.util.Collections.singleton;
import static org.zalando.logbook.core.BodyFilters.defaultValue;
import static org.zalando.logbook.core.Conditions.contentType;
import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonStringProperty;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This maps 'http://localhost:8081/thumbnails/image.jpg'
        registry.addResourceHandler(WebConstants.thumbnailsUrlPattern)
                .addResourceLocations(FileStorageService.ROOT.toUri().toString())
                .setCachePeriod(180); // Cache images for 3 mins

        registry.addResourceHandler(WebConstants.videosUrlPattern)
                .addResourceLocations(FileStorageService.ROOT.toUri().toString())
                .setCachePeriod(3600);

        registry.addResourceHandler(WebConstants.profilePictureUrlPattern)
                .addResourceLocations(FileStorageService.ROOT.toUri().toString())
                .setCachePeriod(3600);
    }
//    @Bean
//    public BodyFilter bodyFilter() {
//        return BodyFilter.merge(
//                defaultValue(),
//                replaceJsonStringProperty(singleton("secret"), "XXX"));
//    }
//@Bean
//public Logbook logbook() {
//    return Logbook.builder()
//            .condition(exclude(
//                    contentType(MimeTypes.OCTET_STREAM, "multipart/form-data", "image/*", "video/*")
//            ))
//            .build();
//}
}