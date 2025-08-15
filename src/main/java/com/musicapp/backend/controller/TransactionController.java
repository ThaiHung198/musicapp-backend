package com.musicapp.backend.controller;

import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.transaction.CreatePaymentRequest;
import com.musicapp.backend.dto.transaction.PaymentResponse;
import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions") // Đổi đường dẫn API cho nhất quán
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Lấy lịch sử giao dịch của người dùng đang đăng nhập.
     */
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

    /**
     * Lấy chi tiết một giao dịch theo ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<TransactionDto>> getTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionDto transaction = transactionService.getTransactionById(id, username);
        return ResponseEntity.ok(BaseResponse.success("Transaction retrieved successfully", transaction));
    }
    @PostMapping("/create-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        PaymentResponse paymentResponse = transactionService.createPaymentUrl(username, request);
        return ResponseEntity.ok(BaseResponse.success("Tạo link thanh toán thành công.", paymentResponse));
    }

    @PostMapping("/create-vnpay-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<PaymentResponse>> createVnpayPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpServletRequest, // <<< Spring sẽ tự động inject HttpServletRequest
            Authentication authentication
    ) {
        String username = authentication.getName();
        PaymentResponse paymentResponse = transactionService.createVnpayPaymentUrl(username, request, httpServletRequest);
        return ResponseEntity.ok(BaseResponse.success("Tạo link thanh toán VNPay thành công.", paymentResponse));
    }

    /**
     * <<< ENDPOINT MỚI: Endpoint IPN cho MoMo.
     * Endpoint này phải là public để MoMo có thể gọi.
     */
    @PostMapping("/momo-ipn")
    public ResponseEntity<Void> handleMomoIpn(@RequestBody Map<String, Object> payload) {
        transactionService.processMomoIpn(payload);
        // Theo tài liệu MoMo, chỉ cần trả về status 204 No Content là đủ.
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vnpay-payment-result")
    public ResponseEntity<Map<String, String>> handleVnpayPaymentResult(
            @RequestParam Map<String, String> allParams
    ) {
        // Gọi service để xử lý logic
        boolean isSuccess = transactionService.processVnpayPaymentResult(allParams);

        // Đoạn này có thể tùy chỉnh để trả về một trang HTML thông báo thành công/thất bại
        // Nhưng để đơn giản, chúng ta sẽ trả về JSON mà VNPay yêu cầu cho IPN
        Map<String, String> response = new HashMap<>();
        if (isSuccess) {
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } else {
            response.put("RspCode", "97"); // Mã lỗi bất kỳ khác 00
            response.put("Message", "Confirm Failed");
        }
        return ResponseEntity.ok(response);
    }

}