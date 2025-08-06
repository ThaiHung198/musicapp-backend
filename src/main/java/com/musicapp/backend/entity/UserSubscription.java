package com.musicapp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType subscriptionType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "auto_renewal")
    @Builder.Default
    private Boolean autoRenewal = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private Set<Transaction> transactions;

    public enum SubscriptionType {
        BASIC("Basic"),
        PREMIUM("Premium"),
        VIP("VIP");

        private final String displayName;

        SubscriptionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public BigDecimal getMonthlyPrice() {
            return switch (this) {
                case BASIC -> new BigDecimal("0.00");    // Free
                case PREMIUM -> new BigDecimal("9.99");  // $9.99/month
                case VIP -> new BigDecimal("19.99");     // $19.99/month
            };
        }

        public int getMaxPremiumSongs() {
            return switch (this) {
                case BASIC -> 0;     // No premium songs
                case PREMIUM -> 100; // 100 premium songs per month
                case VIP -> -1;      // Unlimited
            };
        }
    }

    public enum SubscriptionStatus {
        ACTIVE("Active"),
        EXPIRED("Expired"),
        CANCELLED("Cancelled"),
        SUSPENDED("Suspended");

        private final String displayName;

        SubscriptionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE && 
               endDate.isAfter(LocalDateTime.now());
    }

    public boolean isPremiumOrVip() {
        return subscriptionType == SubscriptionType.PREMIUM || 
               subscriptionType == SubscriptionType.VIP;
    }

    public boolean canAccessPremiumSongs() {
        return isActive() && isPremiumOrVip();
    }
}
