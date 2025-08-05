package com.musicapp.backend.dto.song;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSongRequest {
    
    private String title;
    private String description;
    private String thumbnailPath;
    private List<Long> singerIds;
    private List<Long> tagIds;
}
