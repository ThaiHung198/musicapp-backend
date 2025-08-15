package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.notification.NotificationDto;
import com.musicapp.backend.entity.Notification;
import com.musicapp.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final UserMapper userMapper;

    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        User actor = notification.getActor();

        return NotificationDto.builder()
                .id(notification.getId())
                .actor(actor != null ? userMapper.toUserProfileDto(actor) : null)
                .type(notification.getType().name())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}