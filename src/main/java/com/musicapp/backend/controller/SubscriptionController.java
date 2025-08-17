package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.subscription.SubscriptionDto;
import com.musicapp.backend.dto.subscription.SubscriptionPlanDto;
import com.musicapp.backend.dto.transaction.CreatePaymentRequest;
import com.musicapp.backend.dto.transaction.PaymentResponse;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.service.SubscriptionService;
import com.musicapp.backend.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final TransactionService transactionService;

    @GetMapping("/plans")
    public ResponseEntity<BaseResponse<List<SubscriptionPlanDto>>> getAvailablePlans() {
        List<SubscriptionPlanDto> plans = subscriptionService.getAvailablePlans();
        return ResponseEntity.ok(BaseResponse.success("Lấy danh sách gói thành công.", plans));
    }

    @PostMapping("/create-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpServletRequest,
            @AuthenticationPrincipal User currentUser
    ) {
        String username = currentUser.getUsername();
        PaymentResponse paymentResponse = transactionService.createPaymentUrl(username, request, httpServletRequest);
        return ResponseEntity.ok(BaseResponse.success("Tạo link thanh toán thành công.", paymentResponse));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<SubscriptionDto>> getMySubscription(Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.getUserActiveSubscription(username);
        return ResponseEntity.ok(BaseResponse.success("Lấy thông tin gói đang hoạt động thành công.", subscription));
    }

    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<SubscriptionDto>> cancelSubscription(Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.cancelSubscription(username);
        return ResponseEntity.ok(BaseResponse.success("Hủy gói đăng ký thành công.", subscription));
    }

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

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Object>> getSubscriptionStats() {
        Object stats = subscriptionService.getSubscriptionStats();
        return ResponseEntity.ok(BaseResponse.success("Lấy thống kê thành công.", stats));
    }

    @GetMapping("/admin/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Object>> getSubscriptionRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Object revenue = subscriptionService.getSubscriptionRevenue(startDate, endDate);
        return ResponseEntity.ok(BaseResponse.success("Lấy doanh thu thành công.", revenue));
    }
}