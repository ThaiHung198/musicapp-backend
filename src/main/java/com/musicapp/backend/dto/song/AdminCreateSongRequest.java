package com.musicapp.backend.dto.song;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class AdminCreateSongRequest {

    @NotBlank(message = "Song title is required")
    private String title;

    private String description;

    private String lyrics;

    private List<Long> singerIds;

    @Valid
    private List<NewSingerInfo> newSingers;

    private List<Long> tagIds;

    private List<String> newTags;

    private boolean isPremium = false;

    @Data
    public static class NewSingerInfo {
        @NotBlank(message = "New singer name is required")
        private String name;

        @Email(message = "Invalid email format for new singer")
        private String email;

        private String avatarFileName;
    }
}