package com.musicapp.backend.dto.singer;

import com.musicapp.backend.entity.Singer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingerDto {
    private Long id;
    private String name;
    private String email;
    private String avatarPath;
    private Long songCount;
    private Long creatorId;
    private String creatorName;
    private String status;

    // Constructor này được sử dụng bởi JPQL trong SingerRepository
    // để tạo DTO trực tiếp từ câu lệnh query, giúp tối ưu hiệu năng.
    public SingerDto(Long id, String name, String email, String avatarPath, Long songCount, Long creatorId, String creatorName, Singer.SingerStatus status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatarPath = avatarPath;
        this.songCount = songCount;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        // Chuyển đổi enum thành String ngay trong constructor
        this.status = (status != null) ? status.name() : null;
    }
}