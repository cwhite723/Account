package com.hayan.Account.service;

import com.hayan.Account.domain.*;
import com.hayan.Account.domain.dto.request.CancelUseRequestDto;
import com.hayan.Account.domain.dto.request.UseBalanceRequestDto;
import com.hayan.Account.domain.dto.response.TransactionResponseDto;
import com.hayan.Account.exception.CustomException;
import com.hayan.Account.repository.AccountRepository;
import com.hayan.Account.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.hayan.Account.domain.Transaction.TransactionResult.FAILURE;
import static com.hayan.Account.domain.Transaction.TransactionResult.SUCCESS;
import static com.hayan.Account.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MemberService memberService;

    private Member member;
    private Account account;

    @BeforeEach
    void setUp() {
        member = memberService.getByName("조하얀");
        account = Account.of(member, "0123456789", 10000);
        accountRepository.save(account);
    }

    @AfterEach
    void cleanUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void 이름과_계좌번호와_금액으로_잔액을_사용하면_계좌_잔액이_사용_금액만큼_차감된다() {
        // Given
        UseBalanceRequestDto request = new UseBalanceRequestDto("조하얀", "0123456789", 2000);

        // When
        transactionService.useBalance(request);

        // Then
        Account updatedAccount = accountService.getByAccountNumber("0123456789");
        assertThat(updatedAccount.getBalance()).isEqualTo(8000);
    }

    @Test
    void 잔액을_사용하면_잔액_사용_거래로_저장된다() {
        // Given
        UseBalanceRequestDto request = new UseBalanceRequestDto("조하얀", "0123456789", 2000);

        // When
        TransactionResponseDto response = transactionService.useBalance(request);

        // Then
        assertThat(transactionService.getById(response.transactionId())).isInstanceOf(UseBalanceTransaction.class);
        assertThat(response.accountNumber()).isEqualTo("0123456789");
        assertThat(response.amount()).isEqualTo(2000);
        assertThat(response.transactionResult()).isEqualTo(SUCCESS);
    }

    @Test
    void 거래번호_계좌번호_금액으로_잔액_사용을_취소하면_잔액이_복구된다() {
        // Given
        TransactionResponseDto response = transactionService.useBalance(new UseBalanceRequestDto("조하얀", "0123456789", 2000));
        Transaction originalTransaction = transactionService.getById(response.transactionId());
        CancelUseRequestDto request = new CancelUseRequestDto(originalTransaction.getId(), "0123456789", 2000);


        // When
        transactionService.cancelUse(request);

        // Then
        assertThat(account.getBalance()).isEqualTo(10000);
    }

    @Test
    void 잔액_사용_취소를_하면_취소_거래로_저장한다() {
        // Given
        TransactionResponseDto response = transactionService.useBalance(new UseBalanceRequestDto("조하얀", "0123456789", 2000));
        Transaction originalTransaction = transactionService.getById(response.transactionId());
        CancelUseRequestDto request = new CancelUseRequestDto(originalTransaction.getId(), "0123456789", 2000);

        // When
        TransactionResponseDto cancelResponse = transactionService.cancelUse(request);
        Transaction cancelTransaction = transactionService.getById(cancelResponse.transactionId());

        // Then
        assertThat(transactionService.getById(cancelResponse.transactionId())).isInstanceOf(CancelUseTransaction.class);
        assertThat(cancelResponse.accountNumber()).isEqualTo("0123456789");
        assertThat(cancelResponse.amount()).isEqualTo(2000);
        assertThat(cancelResponse.transactionResult()).isEqualTo(SUCCESS);
    }

    @Test
    void 거래에_실패해도_FAILURE_Result로_저장된다() {
        // Given
        UseBalanceRequestDto request = new UseBalanceRequestDto("조하얀", "0123456789", 12000);

        // When
        TransactionResponseDto response = transactionService.useBalance(request);

        // When & Then
        Transaction transaction = transactionService.getById(response.transactionId());
        assertThat(transaction).isInstanceOf(UseBalanceTransaction.class);
        assertThat(account.getBalance()).isEqualTo(10000);
        assertThat(transaction.getTransactionResult()).isEqualTo(FAILURE);
    }

    @Test
    void 잔액_10000원_계좌에_1000원_사용_요청이_동시에_11번_들어오면_10번만_성공한다() throws InterruptedException {
        // Given
        int numberOfThreads = 11;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        UseBalanceRequestDto request = new UseBalanceRequestDto("조하얀", "0123456789", 1000);

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    transactionService.useBalance(request);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Account updatedAccount = accountService.getByAccountNumber("0123456789");

        List<Transaction> transactions = transactionRepository.findAll();
        long successCount = transactions.stream()
                .filter(transaction -> transaction.getTransactionResult() == Transaction.TransactionResult.SUCCESS)
                .count();
        long failureCount = transactions.stream()
                .filter(transaction -> transaction.getTransactionResult() == Transaction.TransactionResult.FAILURE)
                .count();

        // Then
        assertThat(updatedAccount.getBalance()).isEqualTo(0);
        assertThat(successCount).isEqualTo(10);
        assertThat(failureCount).isEqualTo(1);
    }

    @Test
    void 동일한_잔액_사용_취소_거래가_동시에_들어오면_한_번만_성공한다() throws InterruptedException {
        // Given
        TransactionResponseDto response = transactionService.useBalance(new UseBalanceRequestDto("조하얀", "0123456789", 2000));
        Transaction originalTransaction = transactionService.getById(response.transactionId());

        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        CancelUseRequestDto request = new CancelUseRequestDto(originalTransaction.getId(), "0123456789", 2000);

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    transactionService.cancelUse(request);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        List<CancelUseTransaction> cancelTransactions = transactionRepository.findAll().stream()
                .filter(transaction -> transaction instanceof CancelUseTransaction)
                .map(transaction -> (CancelUseTransaction) transaction)
                .collect(Collectors.toList());

        long successCount = cancelTransactions.stream()
                .filter(transaction -> transaction.getTransactionResult() == Transaction.TransactionResult.SUCCESS)
                .count();

        // Then
        assertThat(successCount).isEqualTo(1);
    }

    @Test
    void 거래번호로_거래를_조회할_수_있다() {
        // Given
        Transaction transaction = new UseBalanceTransaction(account, 3000, SUCCESS);
        transactionRepository.save(transaction);

        // When
        TransactionResponseDto response = transactionService.getTransaction(transaction.getId());

        // Then
        assertThat(response.accountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(response.amount()).isEqualTo(transaction.getAmount());
        assertThat(response.transactionResult()).isEqualTo(transaction.getTransactionResult());
    }

    @Test
    void 잔액_사용_취소_거래를_취소하면_예외가_발생한다() {
        // Given
        Transaction originalTransaction = new UseBalanceTransaction(account, 3000, SUCCESS);
        transactionRepository.save(originalTransaction);
        Transaction cancelUseTransaction = new CancelUseTransaction(account, 3000, originalTransaction, SUCCESS);
        transactionRepository.save(cancelUseTransaction);

        // When & Then
        assertThatThrownBy(() -> transactionService.cancelUse(new CancelUseRequestDto(cancelUseTransaction.getId(), "0123456789", 3000)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UNSUPPORTED_TRANSACTION_TYPE);
    }

    @Test
    void 이미_취소된_거래를_취소하면_예외가_발생한다() {
        // Given
        Transaction originalTransaction = new UseBalanceTransaction(account, 3000, SUCCESS);
        transactionRepository.save(originalTransaction);
        Transaction cancelUseTransaction = new CancelUseTransaction(account, 3000, originalTransaction, SUCCESS);
        transactionRepository.save(cancelUseTransaction);

        // When & Then
        assertThatThrownBy(() -> transactionService.cancelUse(new CancelUseRequestDto(originalTransaction.getId(), "0123456789", 3000)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TRANSACTION_ALREADY_CANCELLED);
    }

    @Test
    void 입력_금액이_원거래_금액과_다르면_예외가_발생한다() {
        // Given
        Transaction transaction = new UseBalanceTransaction(account, 3000, SUCCESS);
        transactionRepository.save(transaction);
        CancelUseRequestDto request = new CancelUseRequestDto(transaction.getId(), "0123456789", 4000);

        // When & Then
        assertThatThrownBy(() -> transactionService.cancelUse(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", AMOUNT_MISMATCH);
    }

    @Test
    void 입력_계좌가_원거래_계좌와_다르면_예외가_발생한다() {
        // Given
        Transaction transaction = new UseBalanceTransaction(account, 3000, SUCCESS);
        transactionRepository.save(transaction);

        // When & Then
        assertThatThrownBy(() -> transactionService.cancelUse(new CancelUseRequestDto(transaction.getId(), "0987654321", 3000)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ACCOUNT_MISMATCH);
    }
}
