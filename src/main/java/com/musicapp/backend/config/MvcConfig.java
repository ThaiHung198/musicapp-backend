// src/main/java/com/musicapp/backend/config/MvcConfig.java
package com.musicapp.backend.config;

import com.musicapp.backend.service.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().toString();

        // Expose a URL path `/uploads/**` that maps to the physical upload directory
        // Ví dụ: file tại /path/to/uploads/audio/song.mp3 sẽ có thể truy cập qua http://localhost:8080/uploads/audio/song.mp3
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}