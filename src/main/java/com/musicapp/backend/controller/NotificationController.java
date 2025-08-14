package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.notification.NotificationDto;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Lấy danh sách thông báo của tôi (phân trang)
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<PagedResponse<NotificationDto>>> getMyNotifications(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10) Pageable pageable) {
        PagedResponse<NotificationDto> response = notificationService.getMyNotifications(currentUser, pageable);
        return ResponseEntity.ok(BaseResponse.success("Lấy danh sách thông báo thành công.", response));
    }

    // Lấy số thông báo chưa đọc
    @GetMapping("/my/unread-count")
    public ResponseEntity<BaseResponse<Map<String, Long>>> getMyUnreadCount(
            @AuthenticationPrincipal User currentUser) {
        Map<String, Long> response = notificationService.getMyUnreadCount(currentUser);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    // Đánh dấu một thông báo là đã đọc
    @PostMapping("/{id}/read")
    public ResponseEntity<BaseResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok(BaseResponse.success("Đã đánh dấu thông báo là đã đọc.", null));
    }

    // Đánh dấu tất cả thông báo là đã đọc
    @PostMapping("/mark-all-as-read")
    public ResponseEntity<BaseResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(BaseResponse.success("Đã đánh dấu tất cả thông báo là đã đọc.", null));
    }
}