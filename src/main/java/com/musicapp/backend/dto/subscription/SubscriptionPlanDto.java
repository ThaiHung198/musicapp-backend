package com.musicapp.backend.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDto {
    private String id;
    private String name;
    private BigDecimal price;
    private int durationDays;
    private String period;
    private List<String> features;
}