package com.musicapp.backend.dto.submission;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubmissionRequest {

    @NotBlank(message = "Song title is required")
    private String title;

    private String description;

    @NotBlank(message = "File path is required")
    private String filePath;

    private String thumbnailPath;

    @Builder.Default
    private Boolean isPremium = false;

    private List<Long> tagIds;

    private List<Long> existingSingerIds;

    // Danh sách thông tin các ca sĩ mới cần tạo và chờ duyệt
    @Valid
    private List<NewSingerInfo> newSingers;

    // DTO con để chứa thông tin ca sĩ mới
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewSingerInfo {
        private Long id;

        @NotBlank(message = "New singer name is required")
        private String name;

        @NotBlank(message = "New singer email is required")
        @Email(message = "Invalid email format for new singer")
        private String email;

        private String avatarPath;
    }
}
