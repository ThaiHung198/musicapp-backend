package com.musicapp.backend.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal price;
    private String status;
    private Boolean autoRenewal;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private Long userId;
    private String userName;
    private String userEmail;
    private Boolean isActive;
    private Boolean isExpiring;
    private Long daysRemaining;
    private Long transactionId;
}