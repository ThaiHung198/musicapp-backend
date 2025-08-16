package com.musicapp.backend.dto.notification;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDto {
    private Long id;
    private String message;
    private String type;
    private boolean isRead;
    private String link;
    private LocalDateTime createdAt;
    private String actorName;
    private String actorAvatarPath;
}