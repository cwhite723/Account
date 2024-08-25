package com.hayan.Account.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hayan.Account.domain.Transaction;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponseDto(
        String accountNumber,
        String transactionType,
        Transaction.TransactionResult transactionResult,
        Long transactionId,
        Integer amount,
        LocalDateTime transactionDate
) { }
