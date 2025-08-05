package com.musicapp.backend.dto.song;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSongRequest {
    
    @NotBlank(message = "Song title is required")
    private String title;
    
    private String description;
    
    @NotBlank(message = "File path is required")
    private String filePath;
    
    private String thumbnailPath;
    
    @NotNull(message = "At least one singer is required")
    private List<Long> singerIds;
    
    private List<Long> tagIds;
}
