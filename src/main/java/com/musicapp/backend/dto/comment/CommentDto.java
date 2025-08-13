package com.musicapp.backend.dto.comment;

import com.musicapp.backend.dto.user.UserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserProfileDto user; //  Nhúng thông tin hồ sơ người dùng

}