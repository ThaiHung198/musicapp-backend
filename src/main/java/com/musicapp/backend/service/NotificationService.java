package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.notification.NotificationDto;
import com.musicapp.backend.entity.Notification;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.exception.UnauthorizedException;
import com.musicapp.backend.mapper.NotificationMapper;
import com.musicapp.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public PagedResponse<NotificationDto> getMyNotifications(User currentUser, Pageable pageable) {
        Page<Notification> notificationPage = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        return PagedResponse.of(notificationPage.map(notificationMapper::toDto).getContent(), notificationPage);
    }

    public Map<String, Long> getMyUnreadCount(User currentUser) {
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(currentUser.getId());
        return Map.of("unreadCount", count);
    }

    @Transactional
    public void markAllAsRead(User currentUser) {
        notificationRepository.markAllAsReadForRecipient(currentUser.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to access this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}