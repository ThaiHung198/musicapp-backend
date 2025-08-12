package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.subscription.SubscriptionDto;
import com.musicapp.backend.service.SubscriptionService;
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
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Lấy danh sách các gói có sẵn để người dùng mua.
     * Endpoint này là bước đầu tiên trong luồng nâng cấp Premium.
     */
    @GetMapping("/packages")
    public ResponseEntity<BaseResponse<Object>> getAvailablePackages() {
        Object packages = subscriptionService.getAvailablePackages();
        return ResponseEntity.ok(BaseResponse.success("Lấy danh sách gói thành công.", packages));
    }

    /**
     * <<< ĐÃ XÓA ENDPOINT POST / (phương thức createSubscription) >>>
     *
     * Lý do: Việc "tạo" một gói đăng ký giờ đây là kết quả của một giao dịch thành công.
     * Luồng đúng sẽ là:
     * 1. Frontend gọi GET /api/v1/subscriptions/packages để hiển thị các gói.
     * 2. Người dùng chọn gói, frontend gọi POST /api/v1/transactions/create-payment.
     * 3. Backend xử lý thanh toán và tự động tạo subscription sau khi thanh toán thành công.
     *
     * Giữ lại endpoint này sẽ gây ra lỗi và nhầm lẫn trong logic.
     */

    /**
     * Lấy gói đăng ký đang hoạt động của người dùng hiện tại.
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<SubscriptionDto>> getMySubscription(Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.getUserActiveSubscription(username);
        return ResponseEntity.ok(BaseResponse.success("Lấy thông tin gói đang hoạt động thành công.", subscription));
    }

    /**
     * Người dùng hủy gói đăng ký đang hoạt động.
     */
    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<SubscriptionDto>> cancelSubscription(Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.cancelSubscription(username);
        return ResponseEntity.ok(BaseResponse.success("Hủy gói đăng ký thành công.", subscription));
    }

    // --- CÁC ENDPOINT DÀNH CHO ADMIN ---
    /**
     * [ADMIN] Lấy tất cả các gói đăng ký trong hệ thống.
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
        return ResponseEntity.ok(BaseResponse.success("Lấy thống kê thành công.", stats));
    }

    /**
     * [ADMIN] Lấy doanh thu.
     */
    @GetMapping("/admin/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Object>> getSubscriptionRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Object revenue = subscriptionService.getSubscriptionRevenue(startDate, endDate);
        return ResponseEntity.ok(BaseResponse.success("Lấy doanh thu thành công.", revenue));
    }
}