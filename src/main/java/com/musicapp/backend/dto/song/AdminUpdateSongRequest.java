package com.musicapp.backend.dto.song;

import lombok.Data;
import java.util.List;

@Data
public class AdminUpdateSongRequest {
    private String title;
    private String description;
    private String thumbnailPath;
    private List<Long> singerIds;
    private List<Long> tagIds;
    private Boolean isPremium;
}