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

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public PagedResponse<NotificationDto> getNotificationsForUser(User user, Pageable pageable) {
        Page<Notification> notificationPage = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);
        Page<NotificationDto> dtoPage = notificationPage.map(notificationMapper::toDto);
        return PagedResponse.of(dtoPage.getContent(), dtoPage);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(User user) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền truy cập thông báo này.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadForRecipient(user.getId());
    }
}