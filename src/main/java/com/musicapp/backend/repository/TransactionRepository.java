// src/main/java/com/musicapp/backend/repository/TransactionRepository.java
package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Transaction;
import com.musicapp.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionCode(String transactionCode);

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserAndStatus(User user, Transaction.TransactionStatus status);
}