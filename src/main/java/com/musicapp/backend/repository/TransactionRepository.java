package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds a transaction by its unique code from a payment gateway.
     * @param transactionCode The unique code of the transaction.
     * @return An Optional containing the transaction if found.
     */
    Optional<Transaction> findByTransactionCode(String transactionCode);

    /**
     * Finds all transactions for a specific user, ordered by most recent first.
     * This is used to display the user's transaction history.
     * @param userId The ID of the user.
     * @param pageable Pagination information.
     * @return A Page of transactions for the user.
     */
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

}