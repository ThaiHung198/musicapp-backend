package com.musicapp.backend.dto.song;

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
    
    // Premium features
    @Builder.Default
    private Boolean isPremium = false;
}
