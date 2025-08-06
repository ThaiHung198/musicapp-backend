package com.musicapp.backend.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    private Long id;
    private String subscriptionType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal price;
    private String status;
    private Boolean autoRenewal;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    
    // User info
    private Long userId;
    private String userName;
    private String userEmail;
    
    // Subscription benefits
    private Integer maxPremiumSongs;
    private Boolean unlimitedPremiumSongs;
    
    // Status indicators
    private Boolean isActive;
    private Boolean isExpiring; // Within 7 days of expiration
    private Long daysRemaining;
    
    // Transaction history
    private List<Long> transactionIds;
}
