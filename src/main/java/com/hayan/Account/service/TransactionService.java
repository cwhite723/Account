package com.hayan.Account.service;

import com.hayan.Account.domain.*;
import com.hayan.Account.domain.dto.request.CancelUseRequestDto;
import com.hayan.Account.domain.dto.request.UseBalanceRequestDto;
import com.hayan.Account.domain.dto.response.TransactionResponseDto;
import com.hayan.Account.exception.CustomException;
import com.hayan.Account.redis.DistributedLock;
import com.hayan.Account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.hayan.Account.domain.Transaction.TransactionResult.FAILURE;
import static com.hayan.Account.domain.Transaction.TransactionResult.SUCCESS;
import static com.hayan.Account.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final MemberService memberService;
    private final AccountService accountService;
    private final AccountValidationService accountValidationService;
    private final TransactionRepository transactionRepository;

    @DistributedLock(key = "#request.account")
    public TransactionResponseDto useBalance(UseBalanceRequestDto request) {
        Member member = memberService.getByName(request.memberName());

        Account account = accountService.getByAccountNumber(request.accountNumber());
        System.out.println(account.getBalance());

        accountValidationService.isAccountOwner(member.getId(), account.getMember().getId());
        accountValidationService.canUseBalance(account, request.amount());

        return executeTransaction(account, request.amount(), "USE_BALANCE", () -> {
            account.useBalance(request.amount());
            return new UseBalanceTransaction(account, request.amount(), SUCCESS);
        });
    }

    @DistributedLock(key = "#request.accountNumber")
    public TransactionResponseDto cancelUse(CancelUseRequestDto request) {
        Transaction originalTransaction = getById(request.transactionId());
        Account account = accountService.getByAccountNumber(originalTransaction.getAccount().getAccountNumber());

        validateCancelRequest(request, originalTransaction, account);

        return executeTransaction(account, request.amount(), "CANCEL_USE_BALANCE", () -> {
            account.cancelUseBalance(request.amount());
            return new CancelUseTransaction(account, request.amount(), originalTransaction, SUCCESS);
        });
    }

    private TransactionResponseDto executeTransaction(Account account, Integer amount, String transactionType, TransactionOperation operation) {
        Transaction transaction = null;

        try {
            transaction = operation.execute();
            transactionRepository.save(transaction);
            System.out.println(transaction.getAccount().getBalance());
        } catch (Exception e) {
            if (transactionType.equals("USE_BALANCE"))
                transaction = new UseBalanceTransaction(account, amount, FAILURE);
            else if (transactionType.equals("CANCEL_USE_BALANCE"))
                transaction = new CancelUseTransaction(account, amount, null, FAILURE);

            transactionRepository.save(transaction);
        }

        return TransactionResponseDto.builder()
                .accountNumber(account.getAccountNumber())
                .transactionResult(transaction.getTransactionResult())
                .transactionId(transaction.getId())
                .amount(amount)
                .transactionDate(transaction.getCreatedAt())
                .build();
    }


    public TransactionResponseDto getTransaction(Long transactionId) {
        Transaction transaction = getById(transactionId);

        return TransactionResponseDto.builder()
                .accountNumber(transaction.getAccount().getAccountNumber())
                .transactionType(transaction.getType())
                .transactionResult(transaction.getTransactionResult())
                .transactionId(transactionId)
                .amount(transaction.getAmount())
                .transactionDate(transaction.getCreatedAt())
                .build();
    }

    public Transaction getById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(TRANSACTION_NOT_FOUND));
    }

    private interface TransactionOperation {
        Transaction execute();
    }

    private void validateCancelRequest(CancelUseRequestDto request, Transaction originalTransaction, Account account) {
        if (originalTransaction instanceof CancelUseTransaction)
            throw new CustomException(UNSUPPORTED_TRANSACTION_TYPE);

        if (transactionRepository.existsByOriginalTransaction(originalTransaction))
            throw new CustomException(TRANSACTION_ALREADY_CANCELLED);

        if (!request.amount().equals(originalTransaction.getAmount()))
            throw new CustomException(AMOUNT_MISMATCH);

        if (!request.accountNumber().equals(account.getAccountNumber()))
            throw new CustomException(ACCOUNT_MISMATCH);
    }
}
