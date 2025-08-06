package com.musicapp.backend.dto.submission;

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
    
    @DecimalMin(value = "0.0", message = "Premium price must be non-negative")
    private BigDecimal premiumPrice;
    
    @NotNull(message = "At least one singer is required")
    private List<Long> singerIds;
    
    private List<Long> tagIds;
}
