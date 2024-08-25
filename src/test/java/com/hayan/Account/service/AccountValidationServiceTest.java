package com.hayan.Account.service;

import com.hayan.Account.exception.CustomException;
import com.hayan.Account.exception.ErrorCode;
import com.hayan.Account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class AccountValidationServiceTest {

    @InjectMocks
    private AccountValidationService accountValidationService;

    @Mock
    private AccountRepository accountRepository;

    @Test
    void 계좌_개수가_10개_이상이면_ACCOUNT_LIMIT_EXCEEDED_예외가_발생한다() {
        // Given
        when(accountRepository.countByMemberId(anyLong()))
                .thenReturn(10);

        // When & Then
        assertThatThrownBy(() -> accountValidationService.checkAccountCreationLimit(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_LIMIT_EXCEEDED);
    }
}