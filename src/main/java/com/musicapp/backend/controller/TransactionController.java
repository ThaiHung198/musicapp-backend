package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.transaction.CreatePaymentRequest;
import com.musicapp.backend.dto.transaction.PaymentResponse;
import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<TransactionDto>> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<TransactionDto> transactions = transactionService.getUserTransactions(username, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<TransactionDto>> getTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionDto transaction = transactionService.getTransactionById(id, username);
        return ResponseEntity.ok(BaseResponse.success("Transaction retrieved successfully", transaction));
    }

    // --- BẮT ĐẦU SỬA ĐỔI ---
    @PostMapping("/create-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpServletRequest,
            Authentication authentication
    ) {
        String username = authentication.getName();
        // Sửa lại: Gọi hàm createPaymentUrl duy nhất trong service
        PaymentResponse paymentResponse = transactionService.createPaymentUrl(username, request, httpServletRequest);
        return ResponseEntity.ok(BaseResponse.success("Tạo link thanh toán thành công.", paymentResponse));
    }
    // --- KẾT THÚC SỬA ĐỔI ---

    @PostMapping("/momo-ipn")
    public ResponseEntity<Void> handleMomoIpn(@RequestBody Map<String, Object> payload) {
        transactionService.processMomoIpn(payload);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vnpay-payment-result")
    public ResponseEntity<Void> handleVnpayPaymentResult(@RequestParam Map<String, String> allParams) {
        transactionService.processVnpayPaymentResult(allParams);
        return ResponseEntity.noContent().build();
    }
}