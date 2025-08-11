package com.musicapp.backend.dto.song;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AdminCreateSongRequest {

    @NotBlank(message = "Song title is required")
    private String title;

    private String description;

    @NotEmpty(message = "At least one singer is required")
    private List<Long> singerIds;

    private List<Long> tagIds;

    private boolean isPremium = false;
}