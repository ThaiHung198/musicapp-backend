package com.musicapp.backend.config;

import com.musicapp.backend.service.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import java.util.ArrayList;
import java.util.List;

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
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Vòng lặp để tìm bộ chuyển đổi JSON mặc định của Spring
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;

                // Lấy danh sách các media type mà converter này đã hỗ trợ
                List<MediaType> supportedMediaTypes = new ArrayList<>(jsonConverter.getSupportedMediaTypes());

                // Thêm hỗ trợ cho application/octet-stream
                // Đây là dòng code quan trọng nhất
                supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);

                // Cập nhật lại danh sách media type cho converter
                jsonConverter.setSupportedMediaTypes(supportedMediaTypes);
            }
        }
    }
}