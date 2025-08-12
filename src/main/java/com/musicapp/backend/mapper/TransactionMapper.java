package com.musicapp.backend.mapper;

import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    public TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDto.TransactionDtoBuilder builder = TransactionDto.builder()
                .id(transaction.getId())
                .packageName(transaction.getPackageName())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .transactionCode(transaction.getTransactionCode())
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .createdAt(transaction.getCreatedAt())
                .userId(transaction.getUser() != null ? transaction.getUser().getId() : null)
                .userName(transaction.getUser() != null ? transaction.getUser().getDisplayName() : null);

        // Kiểm tra xem giao dịch này có liên kết với một gói đăng ký không
        if (transaction.getSubscription() != null) {
            builder.subscriptionId(transaction.getSubscription().getId());
            // Nếu có, lấy ngày hết hạn từ gói đăng ký đó
            builder.subscriptionEndDate(transaction.getSubscription().getEndDate());
        }

        // Hoàn thành việc build DTO
        return builder.build();
    }
}