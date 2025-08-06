package com.musicapp.backend.repository;

import com.musicapp.backend.entity.UserSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    // Find by user
    Page<UserSubscription> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Find user's active subscription
    @Query("SELECT s FROM UserSubscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE' " +
            "AND s.endDate > :now ORDER BY s.endDate DESC")
    Optional<UserSubscription> findActiveSubscription(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Find user's current subscription (active or latest)
    @Query("SELECT s FROM UserSubscription s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<UserSubscription> findUserCurrentSubscription(@Param("userId") Long userId, Pageable pageable);

    // Find by subscription type
    Page<UserSubscription> findBySubscriptionTypeOrderByCreatedAtDesc(UserSubscription.SubscriptionType subscriptionType, Pageable pageable);

    // Find by status
    Page<UserSubscription> findByStatusOrderByCreatedAtDesc(UserSubscription.SubscriptionStatus status, Pageable pageable);

    // Find expiring subscriptions
    @Query("SELECT s FROM UserSubscription s WHERE s.status = 'ACTIVE' AND s.endDate BETWEEN :now AND :endDate ORDER BY s.endDate ASC")
    List<UserSubscription> findExpiringSubscriptions(@Param("now") LocalDateTime now, @Param("endDate") LocalDateTime endDate);

    // Find expired subscriptions that need status update
    @Query("SELECT s FROM UserSubscription s WHERE s.status = 'ACTIVE' AND s.endDate < :now")
    List<UserSubscription> findExpiredActiveSubscriptions(@Param("now") LocalDateTime now);

    // Find subscriptions with auto renewal
    @Query("SELECT s FROM UserSubscription s WHERE s.autoRenewal = true AND s.status = 'ACTIVE' " +
            "AND s.endDate BETWEEN :now AND :renewalDate ORDER BY s.endDate ASC")
    List<UserSubscription> findSubscriptionsForAutoRenewal(@Param("now") LocalDateTime now, @Param("renewalDate") LocalDateTime renewalDate);

    // Count subscriptions by type
    long countBySubscriptionType(UserSubscription.SubscriptionType subscriptionType);

    // Count subscriptions by status
    long countByStatus(UserSubscription.SubscriptionStatus status);

    // Count active subscriptions
    @Query("SELECT COUNT(s) FROM UserSubscription s WHERE s.status = 'ACTIVE' AND s.endDate > :now")
    long countActiveSubscriptions(@Param("now") LocalDateTime now);

    // Count premium users (PREMIUM or VIP)
    @Query("SELECT COUNT(DISTINCT s.user.id) FROM UserSubscription s WHERE s.status = 'ACTIVE' " +
            "AND s.endDate > :now AND s.subscriptionType IN ('PREMIUM', 'VIP')")
    long countPremiumUsers(@Param("now") LocalDateTime now);

    // Recent subscriptions for dashboard
    @Query("SELECT s FROM UserSubscription s ORDER BY s.createdAt DESC")
    List<UserSubscription> findRecentSubscriptions(Pageable pageable);

    // Subscription analytics by date range
    @Query("SELECT s FROM UserSubscription s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    Page<UserSubscription> findSubscriptionsInDateRange(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        Pageable pageable);

    // Check if user has active premium subscription
    @Query("SELECT COUNT(s) > 0 FROM UserSubscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE' " +
            "AND s.endDate > :now AND s.subscriptionType IN ('PREMIUM', 'VIP')")
    boolean hasActivePremiumSubscription(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Find user's active subscriptions (alternative method)
    @Query("SELECT s FROM UserSubscription s WHERE s.user = :user AND s.status = 'ACTIVE' AND s.endDate > :now ORDER BY s.createdAt DESC")
    List<UserSubscription> findUserActiveSubscriptions(@Param("user") com.musicapp.backend.entity.User user, @Param("now") LocalDateTime now);

    // Additional methods needed by SubscriptionService
    List<UserSubscription> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, UserSubscription.SubscriptionStatus status);

    Page<UserSubscription> findBySubscriptionTypeAndStatusAndEndDateAfter(
            UserSubscription.SubscriptionType subscriptionType,
            UserSubscription.SubscriptionStatus status,
            LocalDateTime endDate,
            Pageable pageable);

    Page<UserSubscription> findBySubscriptionTypeAndStatusNot(
            UserSubscription.SubscriptionType subscriptionType,
            UserSubscription.SubscriptionStatus status,
            Pageable pageable);

    Page<UserSubscription> findByStatusAndEndDateAfter(
            UserSubscription.SubscriptionStatus status,
            LocalDateTime endDate,
            Pageable pageable);

    Page<UserSubscription> findByStatusNot(
            UserSubscription.SubscriptionStatus status,
            Pageable pageable);
}