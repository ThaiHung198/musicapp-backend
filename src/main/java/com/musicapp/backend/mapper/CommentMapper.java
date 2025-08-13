package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.comment.CommentDto;
import com.musicapp.backend.dto.user.UserProfileDto;
import com.musicapp.backend.entity.BaseComment;
import com.musicapp.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(BaseComment comment) {
        if (comment == null) {
            return null;
        }

        UserProfileDto userProfileDto = new UserProfileDto();
        User user = comment.getUser();
        if (user != null) {
            userProfileDto.setId(user.getId());
            userProfileDto.setDisplayName(user.getDisplayName());
            userProfileDto.setAvatarPath(user.getAvatarPath());
        }

        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .user(userProfileDto)
                .build();
    }
}