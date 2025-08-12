package com.musicapp.backend.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    @NotBlank(message = "Mã gói không được để trống.")
    private String packageId; // Ví dụ: "monthly_premium", "yearly_premium"
}