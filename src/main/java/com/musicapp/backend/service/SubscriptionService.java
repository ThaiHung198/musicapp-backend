package com.musicapp.backend.service;

import com.musicapp.backend.dto.subscription.CreateSubscriptionRequest;
import com.musicapp.backend.dto.subscription.SubscriptionDto;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.entity.UserSubscription;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.SubscriptionMapper;
import com.musicapp.backend.repository.UserRepository;
import com.musicapp.backend.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @Transactional
    public SubscriptionDto createSubscription(CreateSubscriptionRequest request, User user) {
        // Validate subscription type
        UserSubscription.SubscriptionType subscriptionType;
        try {
            subscriptionType = UserSubscription.SubscriptionType.valueOf(request.getSubscriptionType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid subscription type: " + request.getSubscriptionType());
        }

        // Check if user already has an active subscription
        Optional<UserSubscription> activeSubscription = getActiveSubscription(user.getId());
        if (activeSubscription.isPresent()) {
            throw new BadRequestException("User already has an active subscription");
        }

        // Calculate dates and price
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(request.getDurationMonths());

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .subscriptionType(subscriptionType)
                .startDate(startDate)
                .endDate(endDate)
                .price(subscriptionType.getMonthlyPrice().multiply(new java.math.BigDecimal(request.getDurationMonths())))
                .autoRenewal(request.getAutoRenewal())
                .build();

        UserSubscription savedSubscription = subscriptionRepository.save(subscription);

        // Create payment transaction (simplified - in real app would integrate with payment gateway)
        if (subscription.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            // This would create a transaction and handle payment
            // For now, we'll assume payment is successful
        }

        return subscriptionMapper.toDto(savedSubscription);
    }

    public Optional<SubscriptionDto> getUserActiveSubscription(Long userId) {
        Optional<UserSubscription> subscription = getActiveSubscription(userId);
        return subscription.map(subscriptionMapper::toDto);
    }

    public Page<SubscriptionDto> getUserSubscriptions(Long userId, Pageable pageable) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(subscriptionMapper::toDto);
    }

    public SubscriptionDto getSubscriptionById(Long id, User user) {
        UserSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        // Check permission
        if (!subscription.getUser().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new BadRequestException("You don't have permission to access this subscription");
        }

        return subscriptionMapper.toDto(subscription);
    }

    @Transactional
    public SubscriptionDto cancelSubscription(Long id, User user) {
        UserSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        // Check permission
        if (!subscription.getUser().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new BadRequestException("You don't have permission to cancel this subscription");
        }

        if (subscription.getStatus() != UserSubscription.SubscriptionStatus.ACTIVE) {
            throw new BadRequestException("Can only cancel active subscriptions");
        }

        subscription.setStatus(UserSubscription.SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setAutoRenewal(false);

        UserSubscription updatedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(updatedSubscription);
    }

    @Transactional
    public SubscriptionDto updateAutoRenewal(Long id, Boolean autoRenewal, User user) {
        UserSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        // Check permission
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to modify this subscription");
        }

        if (subscription.getStatus() != UserSubscription.SubscriptionStatus.ACTIVE) {
            throw new BadRequestException("Can only modify active subscriptions");
        }

        subscription.setAutoRenewal(autoRenewal);
        UserSubscription updatedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(updatedSubscription);
    }

    // Premium access checks
    public boolean hasActivePremiumSubscription(Long userId) {
        return subscriptionRepository.hasActivePremiumSubscription(userId, LocalDateTime.now());
    }

    public boolean canAccessPremiumSong(Long userId, Long songId) {
        // In the new model, access to premium songs is solely determined by having an active subscription.
        // The concept of purchasing individual songs has been removed.
        return hasActivePremiumSubscription(userId);
    }

    // Admin methods
    public Page<SubscriptionDto> getAllSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable)
                .map(subscriptionMapper::toDto);
    }

    public Page<SubscriptionDto> getSubscriptionsByType(String type, Pageable pageable) {
        UserSubscription.SubscriptionType subscriptionType;
        try {
            subscriptionType = UserSubscription.SubscriptionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid subscription type: " + type);
        }

        return subscriptionRepository.findBySubscriptionTypeOrderByCreatedAtDesc(subscriptionType, pageable)
                .map(subscriptionMapper::toDto);
    }

    public Page<SubscriptionDto> getSubscriptionsByStatus(String status, Pageable pageable) {
        UserSubscription.SubscriptionStatus subscriptionStatus;
        try {
            subscriptionStatus = UserSubscription.SubscriptionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid subscription status: " + status);
        }

        return subscriptionRepository.findByStatusOrderByCreatedAtDesc(subscriptionStatus, pageable)
                .map(subscriptionMapper::toDto);
    }

    // Scheduled tasks (would be called by scheduled jobs)
    @Transactional
    public void processExpiredSubscriptions() {
        List<UserSubscription> expiredSubscriptions =
                subscriptionRepository.findExpiredActiveSubscriptions(LocalDateTime.now());

        for (UserSubscription subscription : expiredSubscriptions) {
            subscription.setStatus(UserSubscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
        }
    }

    @Transactional
    public void processAutoRenewals() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime renewalDate = now.plusDays(1); // Renew 1 day before expiration

        List<UserSubscription> subscriptionsToRenew =
                subscriptionRepository.findSubscriptionsForAutoRenewal(now, renewalDate);

        for (UserSubscription subscription : subscriptionsToRenew) {
            try {
                // Create new subscription
                CreateSubscriptionRequest renewalRequest = CreateSubscriptionRequest.builder()
                        .subscriptionType(subscription.getSubscriptionType().name())
                        .autoRenewal(subscription.getAutoRenewal())
                        .durationMonths(1)
                        .build();

                createSubscription(renewalRequest, subscription.getUser());

                // Mark current subscription as completed
                subscription.setStatus(UserSubscription.SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(subscription);

            } catch (Exception e) {
                // Log error and continue with other renewals
                // In production, you'd want proper error handling and user notification
            }
        }
    }

    @Transactional
    public void processAutoRenewal(UserSubscription subscription) {
        if (!subscription.getAutoRenewal() || subscription.getStatus() != UserSubscription.SubscriptionStatus.ACTIVE) {
            return;
        }

        try {
            // Create new subscription period
            LocalDateTime newStartDate = subscription.getEndDate();
            LocalDateTime newEndDate = newStartDate.plusMonths(1); // Default to 1 month renewal

            UserSubscription renewedSubscription = UserSubscription.builder()
                    .user(subscription.getUser())
                    .subscriptionType(subscription.getSubscriptionType())
                    .startDate(newStartDate)
                    .endDate(newEndDate)
                    .price(subscription.getSubscriptionType().getMonthlyPrice())
                    .autoRenewal(subscription.getAutoRenewal())
                    .build();

            subscriptionRepository.save(renewedSubscription);

            // Mark old subscription as expired
            subscription.setStatus(UserSubscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);

            // Create payment transaction for renewal
            // In real app, this would charge the user's payment method

        } catch (Exception e) {
            // Log error and disable auto-renewal
            subscription.setAutoRenewal(false);
            subscriptionRepository.save(subscription);
            throw e;
        }
    }

    // Statistics
    public long getActiveSubscriptionCount() {
        return subscriptionRepository.countActiveSubscriptions(LocalDateTime.now());
    }

    public long getPremiumUserCount() {
        return subscriptionRepository.countPremiumUsers(LocalDateTime.now());
    }

    public long getSubscriptionCountByType(String type) {
        UserSubscription.SubscriptionType subscriptionType;
        try {
            subscriptionType = UserSubscription.SubscriptionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid subscription type: " + type);
        }

        return subscriptionRepository.countBySubscriptionType(subscriptionType);
    }

    private Optional<UserSubscription> getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveSubscription(userId, LocalDateTime.now());
    }

    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    // Service methods that controller expects but are missing
    public SubscriptionDto createSubscription(CreateSubscriptionRequest request, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        return createSubscription(request, user);
    }

    public SubscriptionDto getUserActiveSubscription(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        Optional<UserSubscription> subscription = getActiveSubscription(user.getId());
        return subscription.map(subscriptionMapper::toDto).orElse(null);
    }

    public SubscriptionDto cancelSubscription(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        Optional<UserSubscription> activeSubscription = getActiveSubscription(user.getId());
        if (activeSubscription.isEmpty()) {
            throw new BadRequestException("No active subscription found to cancel");
        }

        return cancelSubscription(activeSubscription.get().getId(), user);
    }

    public SubscriptionDto reactivateSubscription(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        // Find the most recently cancelled subscription
        List<UserSubscription> cancelledSubscriptions = subscriptionRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), UserSubscription.SubscriptionStatus.CANCELLED);

        if (cancelledSubscriptions.isEmpty()) {
            throw new BadRequestException("No cancelled subscription found to reactivate");
        }

        UserSubscription subscription = cancelledSubscriptions.get(0);
        subscription.setStatus(UserSubscription.SubscriptionStatus.ACTIVE);
        subscription.setCancelledAt(null);
        subscription.setEndDate(LocalDateTime.now().plusMonths(1)); // Extend for 1 month

        UserSubscription reactivatedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(reactivatedSubscription);
    }

    public boolean hasSubscriptionAccess(String username, UserSubscription.SubscriptionType requiredType) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        Optional<UserSubscription> activeSubscription = getActiveSubscription(user.getId());

        if (activeSubscription.isEmpty()) {
            return false; // No active subscription
        }

        UserSubscription subscription = activeSubscription.get();
        // Check if current subscription tier is equal or higher than required
        return subscription.getSubscriptionType().ordinal() >= requiredType.ordinal();
    }

    public Object getSubscriptionTiers() {
        return Map.of(
                "BASIC", Map.of(
                        "name", "Basic",
                        "price", UserSubscription.SubscriptionType.BASIC.getMonthlyPrice(),
                        "features", List.of("Free songs", "Basic audio quality")
                ),
                "PREMIUM", Map.of(
                        "name", "Premium",
                        "price", UserSubscription.SubscriptionType.PREMIUM.getMonthlyPrice(),
                        "features", List.of("Premium songs", "High audio quality", "No ads")
                ),
                "VIP", Map.of(
                        "name", "VIP",
                        "price", UserSubscription.SubscriptionType.VIP.getMonthlyPrice(),
                        "features", List.of("All premium features", "Exclusive content", "Early access")
                )
        );
    }

    public PagedResponse<SubscriptionDto> getAllSubscriptions(
            UserSubscription.SubscriptionType tier, Boolean isActive, Pageable pageable) {

        Page<UserSubscription> subscriptions;

        if (tier != null && isActive != null) {
            // Filter by both tier and active status
            if (isActive) {
                subscriptions = subscriptionRepository.findBySubscriptionTypeAndStatusAndEndDateAfter(
                        tier, UserSubscription.SubscriptionStatus.ACTIVE, LocalDateTime.now(), pageable);
            } else {
                subscriptions = subscriptionRepository.findBySubscriptionTypeAndStatusNot(
                        tier, UserSubscription.SubscriptionStatus.ACTIVE, pageable);
            }
        } else if (tier != null) {
            subscriptions = subscriptionRepository.findBySubscriptionTypeOrderByCreatedAtDesc(tier, pageable);
        } else if (isActive != null) {
            if (isActive) {
                subscriptions = subscriptionRepository.findByStatusAndEndDateAfter(
                        UserSubscription.SubscriptionStatus.ACTIVE, LocalDateTime.now(), pageable);
            } else {
                subscriptions = subscriptionRepository.findByStatusNot(
                        UserSubscription.SubscriptionStatus.ACTIVE, pageable);
            }
        } else {
            subscriptions = subscriptionRepository.findAll(pageable);
        }

        return createPagedResponse(subscriptions.map(subscriptionMapper::toDto));
    }

    public PagedResponse<SubscriptionDto> getUserSubscriptionHistory(Long userId, Pageable pageable) {
        Page<UserSubscription> subscriptions = subscriptionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return createPagedResponse(subscriptions.map(subscriptionMapper::toDto));
    }

    public Object getSubscriptionStats() {
        return Map.of(
                "totalSubscriptions", subscriptionRepository.count(),
                "activeSubscriptions", getActiveSubscriptionCount(),
                "premiumUsers", getPremiumUserCount(),
                "basicSubscriptions", getSubscriptionCountByType("BASIC"),
                "premiumSubscriptions", getSubscriptionCountByType("PREMIUM"),
                "vipSubscriptions", getSubscriptionCountByType("VIP")
        );
    }

    public Object getSubscriptionRevenue(String startDate, String endDate) {
        // This would typically calculate revenue from transactions
        // For now, return a mock response
        return Map.of(
                "totalRevenue", subscriptionRepository.count() * 9.99, // Mock calculation
                "period", Map.of("start", startDate, "end", endDate),
                "breakdown", Map.of(
                        "PREMIUM", getSubscriptionCountByType("PREMIUM") * 9.99,
                        "VIP", getSubscriptionCountByType("VIP") * 19.99
                )
        );
    }

    public void processSubscriptionRenewals() {
        processAutoRenewals();
    }

    // Helper method to create paged response
    private <T> PagedResponse<T> createPagedResponse(Page<T> page) {
        return PagedResponse.of(page.getContent(), page);
    }
}