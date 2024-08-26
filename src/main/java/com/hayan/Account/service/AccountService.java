package com.hayan.Account.service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.hayan.Account.domain.Account;
import com.hayan.Account.domain.Member;
import com.hayan.Account.domain.dto.request.CloseAccountRequestDto;
import com.hayan.Account.domain.dto.request.CreateAccountRequestDto;
import com.hayan.Account.domain.dto.response.CloseAccountResponseDto;
import com.hayan.Account.domain.dto.response.CreateAccountResponseDto;
import com.hayan.Account.domain.dto.response.GetAccountResponseDto;
import com.hayan.Account.exception.CustomException;
import com.hayan.Account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.hayan.Account.exception.ErrorCode.ACCOUNT_LIMIT_EXCEEDED;
import static com.hayan.Account.exception.ErrorCode.ACCOUNT_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {
    private final MemberService memberService;
    private final AccountRepository accountRepository;

    @Transactional
    public CreateAccountResponseDto createAccount(CreateAccountRequestDto request) {
        Member member = memberService.getByName(request.memberName());
        checkAccountCreationLimit(member.getId());

        String accountNumber = generateAccountNumber();
        Account account = Account.of(member, accountNumber, request.balance());
        accountRepository.save(account);

        return CreateAccountResponseDto.of(request.memberName(), accountNumber, account.getCreatedAt());
    }

    @Transactional
    public CloseAccountResponseDto closeAccount(CloseAccountRequestDto request) {
        Member member = memberService.getByName(request.memberName());
        Account account = getByAccountNumber(request.accountNumber());

        account.close(member.getId());

        return CloseAccountResponseDto.of(request.memberName(), account.getAccountNumber(), account.getUpdatedAt());
    }

    public List<GetAccountResponseDto> getAccount(String memberName) {
        Member member = memberService.getByName(memberName);
        List<Account> accounts = accountRepository.findAllByMemberId(member.getId());

        return accounts.stream()
                .map(account -> GetAccountResponseDto.of(account.getAccountNumber(), account.getBalance(), account.getAccountStatus()))
                .collect(Collectors.toList());
    }

    private String generateAccountNumber() {
        String accountNumber = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, "0123456789".toCharArray(), 10);
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            accountNumber = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, "0123456789".toCharArray(), 10);
        }

        return accountNumber;
    }

    private void checkAccountCreationLimit(Long memberId) {
        if (accountRepository.countByMemberId(memberId) >= 10)
            throw new CustomException(ACCOUNT_LIMIT_EXCEEDED);
    }

    public Account getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));
    }
}
