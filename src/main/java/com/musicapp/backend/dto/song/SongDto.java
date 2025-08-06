package com.musicapp.backend.dto.song;

import com.musicapp.backend.dto.singer.SingerDto;
import com.musicapp.backend.dto.tag.TagDto;
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
public class SongDto {
    private Long id;
    private String title;
    private String description;
    private String filePath;
    private String thumbnailPath;
    private Long listenCount;
    private String status;
    private LocalDateTime createdAt;

    // Premium features
    private Boolean isPremium;
    private Boolean canAccess; // Can current user access this song

    // Creator info
    private Long creatorId;
    private String creatorName;

    // Related entities
    private List<SingerDto> singers;
    private List<TagDto> tags;

    // Interaction counts
    private Long likeCount;
    private Long commentCount;
    private Boolean isLikedByCurrentUser;
}