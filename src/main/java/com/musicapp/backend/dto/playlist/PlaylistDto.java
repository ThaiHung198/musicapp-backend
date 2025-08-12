package com.musicapp.backend.dto.playlist;

import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.entity.Playlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDto {
    private Long id;
    private String name;
    private String thumbnailPath;
    private String visibility;
    private LocalDateTime createdAt;

    private Long creatorId;
    private String creatorName;

    private List<SongDto> songs;
    private Integer songCount;

    private Long likeCount;
    private Long commentCount;
    private Boolean isLikedByCurrentUser;

    public PlaylistDto(Long id, String name, String thumbnailPath, Playlist.PlaylistVisibility visibility, LocalDateTime createdAt, Long creatorId, Long songCount, Long likeCount, Long commentCount) {
        this.id = id;
        this.name = name;
        this.thumbnailPath = thumbnailPath;
        this.visibility = visibility.name();
        this.createdAt = createdAt;
        this.creatorId = creatorId;
        this.songCount = songCount.intValue();
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}