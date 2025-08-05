package com.musicapp.backend.dto.playlist;

import com.musicapp.backend.dto.song.SongDto;
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
    
    // Creator info
    private Long creatorId;
    private String creatorName;
    
    // Songs
    private List<SongDto> songs;
    private Integer songCount;
    
    // Interaction counts
    private Long likeCount;
    private Long commentCount;
    private Boolean isLikedByCurrentUser;
}
