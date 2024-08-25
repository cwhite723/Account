package com.hayan.Account.repository;

import com.hayan.Account.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CancelUseTransaction c WHERE c.originalTransaction = :originalTransaction")
    boolean existsByOriginalTransaction(Transaction originalTransaction);
}
