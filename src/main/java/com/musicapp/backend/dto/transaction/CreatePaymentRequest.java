package com.musicapp.backend.dto.transaction;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotEmpty(message = "Mã gói không được để trống.")
    private String packageId;

    @NotEmpty(message = "Phương thức thanh toán không được để trống.")
    private String paymentMethod;
}