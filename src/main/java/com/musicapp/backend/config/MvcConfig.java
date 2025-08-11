package com.musicapp.backend.config;

import com.musicapp.backend.service.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối đến thư mục upload
        Path uploadDir = Paths.get(fileStorageProperties.getUploadDir());
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // <<< SỬA LỖI: Đảm bảo đường dẫn resource location đúng chuẩn "file:/..." >>>
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}