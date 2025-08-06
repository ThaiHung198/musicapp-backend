package com.musicapp.backend.service;

import com.musicapp.backend.entity.UserSubscription;
import com.musicapp.backend.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionSchedulerService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionService subscriptionService;
    // TransactionService is no longer needed for these scheduled tasks.
    // private final TransactionService transactionService;

    /**
     * Process subscription renewals every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void processSubscriptionRenewals() {
        log.info("Starting subscription renewal process");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime renewalWindow = now.plusDays(1); // Process renewals 1 day before expiry

        List<UserSubscription> subscriptionsToRenew = userSubscriptionRepository
                .findSubscriptionsForAutoRenewal(now, renewalWindow);

        int renewedCount = 0;
        int failedCount = 0;

        for (UserSubscription subscription : subscriptionsToRenew) {
            try {
                subscriptionService.processAutoRenewal(subscription);
                renewedCount++;
                log.info("Successfully renewed subscription for user: {}", subscription.getUser().getId());
            } catch (Exception e) {
                failedCount++;
                log.error("Failed to renew subscription for user: {}, error: {}",
                        subscription.getUser().getId(), e.getMessage());
            }
        }

        log.info("Subscription renewal process completed. Renewed: {}, Failed: {}", renewedCount, failedCount);
    }

    /**
     * Update expired subscriptions every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateExpiredSubscriptions() {
        log.info("Starting expired subscription update process");

        LocalDateTime now = LocalDateTime.now();
        List<UserSubscription> expiredSubscriptions = userSubscriptionRepository
                .findExpiredActiveSubscriptions(now);

        int updatedCount = 0;

        for (UserSubscription subscription : expiredSubscriptions) {
            try {
                subscription.setStatus(UserSubscription.SubscriptionStatus.EXPIRED);
                userSubscriptionRepository.save(subscription);
                updatedCount++;
                log.info("Updated expired subscription for user: {}", subscription.getUser().getId());
            } catch (Exception e) {
                log.error("Failed to update expired subscription for user: {}, error: {}",
                        subscription.getUser().getId(), e.getMessage());
            }
        }

        log.info("Expired subscription update process completed. Updated: {} subscriptions", updatedCount);
    }

    /**
     * Send subscription expiry warnings every day at 10 AM
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendSubscriptionExpiryWarnings() {
        log.info("Starting subscription expiry warning process");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningWindow = now.plusDays(3); // Warn 3 days before expiry

        List<UserSubscription> expiringSubscriptions = userSubscriptionRepository
                .findExpiringSubscriptions(now, warningWindow);

        int warningsSent = 0;

        for (UserSubscription subscription : expiringSubscriptions) {
            try {
                // In a real application, you would send email/push notifications here
                log.info("Subscription expiry warning: User {} subscription expires on {}",
                        subscription.getUser().getId(), subscription.getEndDate());
                warningsSent++;
            } catch (Exception e) {
                log.error("Failed to send expiry warning for user: {}, error: {}",
                        subscription.getUser().getId(), e.getMessage());
            }
        }

        log.info("Subscription expiry warning process completed. Warnings sent: {}", warningsSent);
    }

    /**
     * Generate subscription analytics every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void generateSubscriptionAnalytics() {
        log.info("Starting subscription analytics generation");

        try {
            // In a real application, you would generate and store analytics data
            Object stats = subscriptionService.getSubscriptionStats();
            log.info("Subscription analytics generated successfully: {}", stats);
        } catch (Exception e) {
            log.error("Failed to generate subscription analytics: {}", e.getMessage());
        }
    }
}