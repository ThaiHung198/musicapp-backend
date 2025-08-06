package com.musicapp.backend.controller;

import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}