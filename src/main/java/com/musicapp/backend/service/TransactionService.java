package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.transaction.TransactionDto;
import com.musicapp.backend.entity.Transaction;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.entity.UserSubscription;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.TransactionMapper;
import com.musicapp.backend.repository.TransactionRepository;
import com.musicapp.backend.repository.UserRepository;
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
    private final TransactionMapper transactionMapper;

    /**
     * [THAY ĐỔI] Phương thức này giờ được gọi nội bộ từ SubscriptionService.
     * Nó không còn nhận DTO mà nhận trực tiếp các đối tượng cần thiết.
     */
    @Transactional
    public TransactionDto createTransactionForSubscription(
            User user,
            UserSubscription subscription,
            String packageName,
            String paymentMethod) {

        Transaction transaction = Transaction.builder()
                .user(user)
                .subscription(subscription)
                .packageName(packageName)
                .amount(subscription.getPrice()) // Lấy giá tiền từ gói subscription
                .paymentMethod(paymentMethod)
                .transactionCode("PAY-" + System.currentTimeMillis()) // Tạo mã giao dịch tạm thời
                .status(Transaction.TransactionStatus.SUCCESS) // Mặc định là giao dịch thành công
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDto(savedTransaction);
    }

    /**
     * Phương thức getUserTransactions vẫn giữ nguyên, dùng để lấy lịch sử giao dịch.
     */
    public PagedResponse<TransactionDto> getUserTransactions(String username, Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        Page<Transaction> transactionPage = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        Page<TransactionDto> dtoPage = transactionPage.map(transactionMapper::toDto);

        return PagedResponse.of(dtoPage.getContent(), dtoPage);
    }

    /**
     * Phương thức getTransactionById vẫn giữ nguyên.
     */
    public TransactionDto getTransactionById(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getEmail().equals(username)) {
            throw new SecurityException("User does not have permission to view this transaction.");
        }

        return transactionMapper.toDto(transaction);
    }
}