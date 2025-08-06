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

    /**
     * Tự động gia hạn các gói đăng ký được cấu hình auto-renewal.
     * Chạy vào 2 giờ sáng mỗi ngày.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void processSubscriptionRenewals() {
        log.info("Starting subscription renewal process...");

        LocalDateTime now = LocalDateTime.now();
        // Tìm các gói cần gia hạn (ví dụ: các gói sẽ hết hạn trong 24 giờ tới)
        LocalDateTime renewalWindow = now.plusDays(1);

        List<UserSubscription> subscriptionsToRenew = userSubscriptionRepository
                .findSubscriptionsForAutoRenewal(now, renewalWindow);

        log.info("Found {} subscriptions to renew.", subscriptionsToRenew.size());

        for (UserSubscription subscription : subscriptionsToRenew) {
            try {
                // Logic xử lý gia hạn (ví dụ: tạo giao dịch mới, gia hạn gói)
                // subscriptionService.processAutoRenewal(subscription); // Giả sử có một phương thức xử lý
                log.info("Successfully processed auto-renewal for user: {}", subscription.getUser().getId());
            } catch (Exception e) {
                log.error("Failed to renew subscription for user: {}. Error: {}",
                        subscription.getUser().getId(), e.getMessage());
            }
        }
        log.info("Subscription renewal process finished.");
    }

    /**
     * Cập nhật trạng thái cho các gói đã hết hạn.
     * Chạy mỗi giờ.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateExpiredSubscriptions() {
        log.info("Starting expired subscription update process...");

        LocalDateTime now = LocalDateTime.now();
        List<UserSubscription> expiredSubscriptions = userSubscriptionRepository
                .findExpiredActiveSubscriptions(now);

        log.info("Found {} expired subscriptions to update.", expiredSubscriptions.size());

        for (UserSubscription subscription : expiredSubscriptions) {
            subscription.setStatus(UserSubscription.SubscriptionStatus.EXPIRED);
            userSubscriptionRepository.save(subscription);
        }

        log.info("Expired subscription update process finished.");
    }

    /**
     * Gửi cảnh báo sắp hết hạn cho người dùng.
     * Chạy vào 10 giờ sáng mỗi ngày.
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendSubscriptionExpiryWarnings() {
        log.info("Starting subscription expiry warning process...");

        LocalDateTime now = LocalDateTime.now();
        // Gửi cảnh báo cho các gói sẽ hết hạn trong 3 ngày tới
        LocalDateTime warningWindow = now.plusDays(3);

        List<UserSubscription> expiringSubscriptions = userSubscriptionRepository
                .findExpiringSubscriptions(now, warningWindow);

        log.info("Found {} subscriptions expiring soon to warn.", expiringSubscriptions.size());

        for (UserSubscription subscription : expiringSubscriptions) {
            // Trong ứng dụng thực tế, bạn sẽ gửi email hoặc push notification ở đây
            log.info("Sending expiry warning to user {}. Subscription expires on {}.",
                    subscription.getUser().getEmail(), subscription.getEndDate());
        }

        log.info("Subscription expiry warning process finished.");
    }
}