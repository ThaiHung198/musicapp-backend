package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.comment.CommentDto;
import com.musicapp.backend.dto.user.UserProfileDto;
import com.musicapp.backend.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        UserProfileDto userProfileDto = new UserProfileDto();
        if (comment.getUser() != null) {
            userProfileDto.setId(comment.getUser().getId());
            userProfileDto.setDisplayName(comment.getUser().getDisplayName());
            userProfileDto.setAvatarPath(comment.getUser().getAvatarPath());
        }

        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .user(userProfileDto)
                .build();
    }
}