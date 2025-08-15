package com.musicapp.backend.dto.notification;

import com.musicapp.backend.dto.user.UserProfileDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDto {
    private Long id;
    private UserProfileDto actor; // Người thực hiện hành động
    private String type;
    private String message;
    private boolean isRead;
    private String link;
    private LocalDateTime createdAt;
}