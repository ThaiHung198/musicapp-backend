package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.entity.Transaction;
import com.musicapp.backend.entity.UserSubscription;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        UserSubscription subscription = transaction.getSubscription();

        return TransactionDto.builder()
                .id(transaction.getId())
                .packageName(transaction.getPackageName())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .transactionCode(transaction.getTransactionCode())
                .status(transaction.getStatus().name())
                .createdAt(transaction.getCreatedAt())
                .premiumStartDate(subscription != null ? subscription.getStartDate() : null)
                .premiumEndDate(subscription != null ? subscription.getEndDate() : null)
                .build();
    }
}