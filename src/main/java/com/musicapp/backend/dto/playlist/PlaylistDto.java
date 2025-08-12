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
    private Long commentCount; // Thuộc tính này vẫn giữ lại để Service có thể set giá trị sau
    private Boolean isLikedByCurrentUser;

    // --- BẮT ĐẦU SỬA LỖI ---
    // Constructor này được sử dụng bởi PlaylistRepository.
    // Chúng ta đã loại bỏ tham số "commentCount" vì không thể truy vấn trực tiếp được nữa.
    public PlaylistDto(Long id, String name, String thumbnailPath, Playlist.PlaylistVisibility visibility, LocalDateTime createdAt, Long creatorId, Long songCount, Long likeCount) {
        this.id = id;
        this.name = name;
        this.thumbnailPath = thumbnailPath;
        this.visibility = visibility.name();
        this.createdAt = createdAt;
        this.creatorId = creatorId;
        this.songCount = songCount.intValue();
        this.likeCount = likeCount;
        // this.commentCount sẽ được set thủ công ở tầng Service nếu cần.
    }
    // --- KẾT THÚC SỬA LỖI ---
}