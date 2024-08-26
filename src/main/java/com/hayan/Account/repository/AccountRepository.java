package com.hayan.Account.repository;

import com.hayan.Account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Integer countByMemberId(Long memberId);
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findAllByMemberId(Long memberId);
}
