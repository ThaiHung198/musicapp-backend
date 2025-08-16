package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.subscription.SubscriptionDto;
import com.musicapp.backend.entity.Transaction;
import com.musicapp.backend.entity.UserSubscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {

    public SubscriptionDto toDto(UserSubscription subscription) {
        if (subscription == null) return null;

        LocalDateTime now = LocalDateTime.now();
        boolean isActive = subscription.isActive();
        boolean isExpiring = isActive && subscription.getEndDate().isBefore(now.plusDays(7));
        long daysRemaining = isActive ? ChronoUnit.DAYS.between(now, subscription.getEndDate()) : 0;

        Transaction transaction = subscription.getTransaction();
        Long transactionId = (transaction != null) ? transaction.getId() : null;

        return SubscriptionDto.builder()
                .id(subscription.getId())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .price(subscription.getPrice())
                .status(subscription.getStatus().name())
                .autoRenewal(subscription.getAutoRenewal())
                .createdAt(subscription.getCreatedAt())
                .cancelledAt(subscription.getCancelledAt())
                .userId(subscription.getUser().getId())
                .userName(subscription.getUser().getDisplayName())
                .userEmail(subscription.getUser().getEmail())
                .isActive(isActive)
                .isExpiring(isExpiring)
                .daysRemaining(daysRemaining)
                .transactionId(transactionId)
                .build();
    }

    public SubscriptionDto toDtoBasic(UserSubscription subscription) {
        if (subscription == null) return null;

        LocalDateTime now = LocalDateTime.now();
        boolean isActive = subscription.isActive();
        long daysRemaining = isActive ? ChronoUnit.DAYS.between(now, subscription.getEndDate()) : 0;
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus().name())
                .isActive(isActive)
                .daysRemaining(daysRemaining)
                .build();
    }
}