package com.musicapp.backend.controller;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.subscription.CreateSubscriptionRequest;
import com.musicapp.backend.dto.subscription.SubscriptionDto;
import com.musicapp.backend.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor // Sử dụng @RequiredArgsConstructor thay cho @Autowired
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Lấy danh sách các gói có sẵn để người dùng mua.
     * Endpoint này thay thế cho getSubscriptionTiers() cũ.
     */
    @GetMapping("/packages")
    public ResponseEntity<BaseResponse<Object>> getAvailablePackages() {
        Object packages = subscriptionService.getAvailablePackages();
        return ResponseEntity.ok(BaseResponse.success("Available packages retrieved", packages));
    }

    /**
     * Người dùng tạo một gói đăng ký mới bằng cách gửi packageId.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<SubscriptionDto>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.createSubscription(request, username);
        return ResponseEntity.ok(BaseResponse.success("Subscription created successfully", subscription));
    }

    /**
     * Lấy gói đăng ký đang hoạt động của người dùng hiện tại.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<SubscriptionDto>> getMySubscription(Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.getUserActiveSubscription(username);
        return ResponseEntity.ok(BaseResponse.success("Subscription retrieved successfully", subscription));
    }

    /**
     * Người dùng hủy gói đăng ký đang hoạt động.
     */
    @PostMapping("/cancel")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<SubscriptionDto>> cancelSubscription(Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.cancelSubscription(username);
        return ResponseEntity.ok(BaseResponse.success("Subscription cancelled successfully", subscription));
    }

    // --- CÁC ENDPOINT DÀNH CHO ADMIN ---

    /**
     * [ADMIN] Lấy tất cả các gói đăng ký trong hệ thống.
     * Đã loại bỏ tham số 'tier' không còn hợp lệ.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<SubscriptionDto>> getAllSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<SubscriptionDto> subscriptions = subscriptionService.getAllSubscriptions(pageable, isActive);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * [ADMIN] Lấy lịch sử đăng ký của một người dùng cụ thể.
     */
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<SubscriptionDto>> getUserSubscriptionHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<SubscriptionDto> subscriptions = subscriptionService.getUserSubscriptionHistory(userId, pageable);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * [ADMIN] Lấy các số liệu thống kê về gói đăng ký.
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Object>> getSubscriptionStats() {
        Object stats = subscriptionService.getSubscriptionStats();
        return ResponseEntity.ok(BaseResponse.success("Subscription statistics retrieved", stats));
    }

    /**
     * [ADMIN] Lấy doanh thu (cần triển khai logic chi tiết trong service).
     * Cập nhật để nhận kiểu dữ liệu LocalDateTime.
     */
    @GetMapping("/admin/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Object>> getSubscriptionRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Object revenue = subscriptionService.getSubscriptionRevenue(startDate, endDate);
        return ResponseEntity.ok(BaseResponse.success("Subscription revenue retrieved", revenue));
    }
}