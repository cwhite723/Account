package com.hayan.Account.service;

import com.hayan.Account.domain.Account;
import com.hayan.Account.domain.Member;
import com.hayan.Account.domain.dto.request.CloseAccountRequestDto;
import com.hayan.Account.domain.dto.request.CreateAccountRequestDto;
import com.hayan.Account.domain.dto.response.CreateAccountResponseDto;
import com.hayan.Account.domain.dto.response.GetAccountResponseDto;
import com.hayan.Account.exception.CustomException;
import com.hayan.Account.exception.ErrorCode;
import com.hayan.Account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.hayan.Account.domain.Account.AccountStatus.UNREGISTERED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@SpringBootTest
@Transactional
class AccountServiceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MemberService memberService;

    @Test
    void 이름과_초기_잔액으로_계좌를_생성할_수_있다() {
        // Given
        var createAccountRequest = new CreateAccountRequestDto("조하얀", 1000);

        // When
        CreateAccountResponseDto response = accountService.createAccount(createAccountRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.memberName()).isEqualTo("조하얀");
        assertThat(response.accountNumber()).isNotNull();

        Account createdAccount = accountRepository.findByAccountNumber(response.accountNumber())
                .orElseThrow();
        assertThat(createdAccount.getBalance()).isEqualTo(1000);
    }

    @Test
    void 이름과_계좌번호로_계좌를_해지할_수_있다() {
        // Given
        Member member = memberService.getByName("조하얀");
        Account account = Account.of(member, "0123456789", 0);
        accountRepository.save(account);
        var closeAccountRequest = new CloseAccountRequestDto(member.getName(), account.getAccountNumber());

        // When
        accountService.closeAccount(closeAccountRequest);

        // Then
        assertThat(account.getAccountStatus()).isEqualTo(UNREGISTERED);
    }

    @Test
    void 이름으로_해당_사용자의_계좌를_List로_받을_수_있다() {
        // Given
        Member member = memberService.getByName("조하얀");
        Account account1 = Account.of(member, "0123456789", 0);
        Account account2 = Account.of(member, "1123456789", 500);
        Account account3 = Account.of(member, "2123456789", 1000);

        Member otherMember = memberService.getByName("김윤정");
        Account account4 = Account.of(otherMember, "3123456789", 500);
        Account account5 = Account.of(otherMember, "4123456789", 1000);

        accountRepository.save(account1);
        accountRepository.save(account2);
        accountRepository.save(account3);
        accountRepository.save(account4);
        accountRepository.save(account5);

        // When
        List<GetAccountResponseDto> accountResponses = accountService.getAccount(member.getName());

        // Then
        assertThat(accountResponses)
                .hasSize(3)
                .extracting("accountNumber", "balance")
                .containsExactlyInAnyOrder(
                        tuple("0123456789", 0),
                        tuple("1123456789", 500),
                        tuple("2123456789", 1000)
                );
    }

    @Test
    void 잔액이_남아_있는_계좌를_해지하면_예외가_발생한다() {
        // Given
        Member member = memberService.getByName("조하얀");
        Account account = Account.of(member, "0123456789", 500);
        accountRepository.save(account);
        var closeAccountRequest = new CloseAccountRequestDto(member.getName(), account.getAccountNumber());

        // When & Then
        assertThatThrownBy(() -> accountService.closeAccount(closeAccountRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BALANCE_REMAINING_FOR_CLOSE);
    }

    @Test
    void 이미_해지된_계좌를_해지하면_예외가_발생한다() {
        // Given
        Member member = memberService.getByName("조하얀");
        Account account = Account.of(member, "0123456789", 0);
        account.close(member.getId());
        accountRepository.save(account);
        var closeAccountRequest = new CloseAccountRequestDto(member.getName(), account.getAccountNumber());

        // When & Then
        assertThatThrownBy(() -> accountService.closeAccount(closeAccountRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_ALREADY_CLOSED);
    }
}

