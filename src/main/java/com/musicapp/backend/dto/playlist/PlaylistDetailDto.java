package com.musicapp.backend.dto.playlist;

import com.musicapp.backend.dto.song.SongDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PlaylistDetailDto {
    private Long id;
    private String name;
    private String thumbnailPath;
    private String visibility;
    private LocalDateTime createdAt;
    private Long creatorId;
    private String creatorName;
    private int songCount;
    private long likeCount;
    private boolean isLikedByCurrentUser;
    private List<SongDto> songs;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canToggleVisibility;
    private Long listenCount;
}