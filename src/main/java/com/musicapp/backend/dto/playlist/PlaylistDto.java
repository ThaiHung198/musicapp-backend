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

    // --- BẮT ĐẦU SỬA LỖI ---
    // XÓA CONSTRUCTOR CŨ GÂY LỖI

    /**
     * Constructor mới này được sử dụng bởi PlaylistRepository.
     * Nó chỉ nhận các tham số mà câu truy vấn JPQL có thể cung cấp trực tiếp.
     * Like count và comment count sẽ được tính và set ở tầng Service.
     */
    public PlaylistDto(Long id, String name, String thumbnailPath, Playlist.PlaylistVisibility visibility, LocalDateTime createdAt, Long creatorId, Long songCount) {
        this.id = id;
        this.name = name;
        this.thumbnailPath = thumbnailPath;
        this.visibility = visibility.name();
        this.createdAt = createdAt;
        this.creatorId = creatorId;
        this.songCount = songCount.intValue();
        // likeCount và commentCount sẽ là null ban đầu
    }
    // --- KẾT THÚC SỬA LỖI ---
}