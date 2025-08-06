package com.musicapp.backend.dto.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {
    
    @NotNull(message = "Subscription type is required")
    private String subscriptionType; // BASIC, PREMIUM, VIP
    
    @Builder.Default
    private Boolean autoRenewal = false;
    
    @Builder.Default
    private Integer durationMonths = 1; // Default 1 month
}
