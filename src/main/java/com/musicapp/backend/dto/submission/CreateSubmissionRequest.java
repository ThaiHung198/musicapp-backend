// src/main/java/com/musicapp/backend/dto/submission/CreateSubmissionRequest.java
package com.musicapp.backend.dto.submission;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubmissionRequest {

    @NotBlank(message = "Song title is required")
    private String title;

    private String description;

    // <<< ĐÃ XÓA: filePath và thumbnailPath vì chúng sẽ được tạo từ file upload

    @Builder.Default
    private Boolean isPremium = false;

    private List<Long> tagIds;
    private List<Long> existingSingerIds;

    @Valid
    private List<NewSingerInfo> newSingers;

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