package com.musicapp.backend.dto.singer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSingerRequest {
    
    @NotBlank(message = "Singer name is required")
    private String name;
    
    private String avatarPath;
}
