package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.transaction.CreateTransactionRequest;
import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.entity.Transaction;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.entity.UserSubscription;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.TransactionMapper;
import com.musicapp.backend.repository.TransactionRepository;
import com.musicapp.backend.repository.UserRepository;
import com.musicapp.backend.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionDto createTransaction(String username, CreateTransactionRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        UserSubscription subscription = userSubscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + request.getSubscriptionId()));

        Transaction transaction = Transaction.builder()
                .user(user)
                .subscription(subscription)
                .packageName(request.getPackageName())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionCode(request.getTransactionCode())
                .status(Transaction.TransactionStatus.SUCCESS) // Assume payment is successful for this base version
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDto(savedTransaction);
    }

    public PagedResponse<TransactionDto> getUserTransactions(String username, Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        Page<Transaction> transactionPage = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        Page<TransactionDto> dtoPage = transactionPage.map(transactionMapper::toDto);

        return PagedResponse.of(dtoPage.getContent(), dtoPage);
    }

    public TransactionDto getTransactionById(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getEmail().equals(username)) {
            throw new SecurityException("User does not have permission to view this transaction.");
        }

        return transactionMapper.toDto(transaction);
    }
}