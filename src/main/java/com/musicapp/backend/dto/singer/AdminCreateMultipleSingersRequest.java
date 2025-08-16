// Tạo file mới tại: src/main/java/com/musicapp/backend/dto/singer/AdminCreateMultipleSingersRequest.java
package com.musicapp.backend.dto.singer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AdminCreateMultipleSingersRequest {

    @NotEmpty(message = "Singer list cannot be empty")
    @Valid // Đảm bảo các đối tượng trong list được validate
    private List<SingerInfo> singers;

    @Data
    public static class SingerInfo {
        @NotEmpty(message = "Client ID is required")
        private String clientId; // ID tạm thời từ client để map với file ảnh

        @NotEmpty(message = "Singer name is required")
        private String name;

        @NotEmpty(message = "Email is required")
        private String email;
    }
}