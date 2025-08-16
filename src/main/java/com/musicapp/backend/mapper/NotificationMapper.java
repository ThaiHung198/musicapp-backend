package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.notification.NotificationDto;
import com.musicapp.backend.entity.Notification;
import com.musicapp.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        User actor = notification.getActor();
        String actorName = (actor != null) ? actor.getDisplayName() : "Hệ thống";
        String actorAvatarPath = (actor != null) ? actor.getAvatarPath() : null;

        return NotificationDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .isRead(notification.isRead())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .actorName(actorName)
                .actorAvatarPath(actorAvatarPath)
                .build();
    }
}