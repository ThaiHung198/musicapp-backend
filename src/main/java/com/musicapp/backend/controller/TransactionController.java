package com.musicapp.backend.controller;

import com.musicapp.backend.dto.transaction.CreateTransactionRequest;
import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.dto.BaseResponse;
import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<TransactionDto>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TransactionDto transaction = transactionService.createTransaction(username, request);
        return ResponseEntity.ok(BaseResponse.success("Transaction created successfully", transaction));
    }

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
}