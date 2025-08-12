package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.subscription.SubscriptionDto;
import com.musicapp.backend.entity.Transaction; // <<< ĐÃ THÊM IMPORT
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

import java.math.BigDecimal;
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
    private final UserRepository userRepository;

    @Transactional
    public void activateSubscriptionFromTransaction(Transaction transaction) {
        if (transaction.getStatus() != Transaction.TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Chỉ có thể kích hoạt gói từ giao dịch thành công.");
        }
        User user = transaction.getUser();

        if (hasActivePremiumSubscription(user.getId())) {
            return;
        }

        Map<String, Object> packageInfo = getPackageInfoByPackageName(transaction.getPackageName());
        int durationInDays = (int) packageInfo.get("durationDays");

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(durationInDays))
                .price(transaction.getAmount())
                .status(UserSubscription.SubscriptionStatus.ACTIVE)
                .autoRenewal(false)
                .build();
        UserSubscription savedSubscription = subscriptionRepository.save(subscription);

        transaction.setSubscription(savedSubscription);
    }

    public boolean hasActivePremiumSubscription(Long userId) {
        return subscriptionRepository.findActiveSubscription(userId, LocalDateTime.now()).isPresent();
    }

    public SubscriptionDto getUserActiveSubscription(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        Optional<UserSubscription> subscription = subscriptionRepository.findActiveSubscription(user.getId(), LocalDateTime.now());
        return subscription.map(subscriptionMapper::toDto).orElse(null);
    }

    @Transactional
    public SubscriptionDto cancelSubscription(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
        UserSubscription activeSubscription = subscriptionRepository.findActiveSubscription(user.getId(), LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("No active subscription found to cancel"));
        activeSubscription.setStatus(UserSubscription.SubscriptionStatus.CANCELLED);
        activeSubscription.setCancelledAt(LocalDateTime.now());
        activeSubscription.setAutoRenewal(false);
        UserSubscription updatedSubscription = subscriptionRepository.save(activeSubscription);
        return subscriptionMapper.toDto(updatedSubscription);
    }

    public Map<String, Object> getAvailablePackages() {
        return Map.of(
                "monthly_premium", getPackageInfo("monthly_premium"),
                "yearly_premium", getPackageInfo("yearly_premium")
        );
    }

    public Map<String, Object> getPackageInfo(String packageId) {
        if ("monthly_premium".equals(packageId)) {
            return Map.of(
                    "name", "Gói Premium 1 Tháng",
                    "price", new BigDecimal("49000.00"),
                    "durationDays", 30,
                    "features", List.of("Nghe nhạc không quảng cáo", "Chất lượng cao")
            );
        } else if ("yearly_premium".equals(packageId)) {
            return Map.of(
                    "name", "Gói Premium 1 Năm",
                    "price", new BigDecimal("499000.00"),
                    "durationDays", 365,
                    "features", List.of("Tất cả quyền lợi của gói Tháng", "Tiết kiệm hơn")
            );
        } else {
            throw new BadRequestException("Mã gói không hợp lệ: " + packageId);
        }
    }

    private Map<String, Object> getPackageInfoByPackageName(String packageName) {
        if ("Gói Premium 1 Tháng".equals(packageName)) {
            return getPackageInfo("monthly_premium");
        } else if ("Gói Premium 1 Năm".equals(packageName)) {
            return getPackageInfo("yearly_premium");
        } else {
            throw new BadRequestException("Tên gói không hợp lệ: " + packageName);
        }
    }

    // <<< ĐÃ XÓA KHỐI CODE BỊ THỪA GÂY LỖI CÚ PHÁP >>>

    // --- Các phương thức Admin và helper giữ nguyên ---
    public PagedResponse<SubscriptionDto> getAllSubscriptions(Pageable pageable, Boolean isActive) {
        Page<UserSubscription> subscriptions;
        if (isActive != null) {
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
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        Page<UserSubscription> subscriptions = subscriptionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return createPagedResponse(subscriptions.map(subscriptionMapper::toDto));
    }

    public Object getSubscriptionStats() {
        long totalSubscriptions = subscriptionRepository.count();
        long activeSubscriptions = subscriptionRepository.countActiveSubscriptions(LocalDateTime.now());
        return Map.of(
                "totalSubscriptions", totalSubscriptions,
                "activeSubscriptions", activeSubscriptions,
                "expiredOrCancelledSubscriptions", totalSubscriptions - activeSubscriptions
        );
    }

    public Object getSubscriptionRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return Map.of(
                "totalRevenue", "Chưa được triển khai",
                "period", Map.of("start", startDate, "end", endDate)
        );
    }

    private <T> PagedResponse<T> createPagedResponse(Page<T> page) {
        return PagedResponse.of(page.getContent(), page);
    }
}