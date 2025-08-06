package com.musicapp.backend.dto.transaction;

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
public class TransactionDto {
    private Long id;
    private String packageName;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionCode;
    private String status;
    private LocalDateTime createdAt;

    // User info
    private Long userId;
    private String userName;

    // Related entity info
    private Long subscriptionId;
}