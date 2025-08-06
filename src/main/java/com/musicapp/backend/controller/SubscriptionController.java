package com.musicapp.backend.controller;

import com.musicapp.backend.dto.subscription.CreateSubscriptionRequest;
import com.musicapp.backend.dto.subscription.SubscriptionDto;
import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.service.SubscriptionService;
import com.musicapp.backend.entity.UserSubscription.SubscriptionType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SubscriptionDto>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.createSubscription(request, username);
        return ResponseEntity.ok(BaseResponse.success("Subscription created successfully", subscription));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SubscriptionDto>> getMySubscription(Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.getUserActiveSubscription(username);
        return ResponseEntity.ok(BaseResponse.success("Subscription retrieved successfully", subscription));
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SubscriptionDto>> cancelSubscription(Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.cancelSubscription(username);
        return ResponseEntity.ok(BaseResponse.success("Subscription cancelled successfully", subscription));
    }

    @PostMapping("/reactivate")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<SubscriptionDto>> reactivateSubscription(Authentication authentication) {
        String username = authentication.getName();
        SubscriptionDto subscription = subscriptionService.reactivateSubscription(username);
        return ResponseEntity.ok(BaseResponse.success("Subscription reactivated successfully", subscription));
    }

    @GetMapping("/check-access")
    @PreAuthorize("hasRole('USER') or hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Boolean>> checkPremiumAccess(
            @RequestParam SubscriptionType requiredTier,
            Authentication authentication) {
        String username = authentication.getName();
        boolean hasAccess = subscriptionService.hasSubscriptionAccess(username, requiredTier);
        return ResponseEntity.ok(BaseResponse.success("Access check completed", hasAccess));
    }

    @GetMapping("/tiers")
    public ResponseEntity<BaseResponse<Object>> getSubscriptionTiers() {
        Object tiers = subscriptionService.getSubscriptionTiers();
        return ResponseEntity.ok(BaseResponse.success("Subscription tiers retrieved", tiers));
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<SubscriptionDto>> getAllSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) SubscriptionType tier,
            @RequestParam(required = false) Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<SubscriptionDto> subscriptions = subscriptionService.getAllSubscriptions(tier, isActive, pageable);
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
        return ResponseEntity.ok(BaseResponse.success("Subscription statistics retrieved", stats));
    }

    @GetMapping("/admin/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Object>> getSubscriptionRevenue(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Object revenue = subscriptionService.getSubscriptionRevenue(startDate, endDate);
        return ResponseEntity.ok(BaseResponse.success("Subscription revenue retrieved", revenue));
    }

    @PostMapping("/admin/manual-renewal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> processManualRenewal() {
        subscriptionService.processSubscriptionRenewals();
        return ResponseEntity.ok(BaseResponse.success("Manual renewal process completed", null));
    }
}
