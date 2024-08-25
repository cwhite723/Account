package com.hayan.Account.controller;

import com.hayan.Account.common.ApplicationResponse;
import com.hayan.Account.domain.dto.request.CloseAccountRequestDto;
import com.hayan.Account.domain.dto.request.CreateAccountRequestDto;
import com.hayan.Account.domain.dto.response.CloseAccountResponseDto;
import com.hayan.Account.domain.dto.response.CreateAccountResponseDto;
import com.hayan.Account.domain.dto.response.GetAccountResponseDto;
import com.hayan.Account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/create")
    public ApplicationResponse<CreateAccountResponseDto> createAccount(@Valid @RequestBody CreateAccountRequestDto request) {
        CreateAccountResponseDto response = accountService.createAccount(request);

        return ApplicationResponse.ok(response);
    }

    @PostMapping("/close")
    public ApplicationResponse<CloseAccountResponseDto> closeAccount(@Valid @RequestBody CloseAccountRequestDto request) {
        CloseAccountResponseDto response = accountService.closeAccount(request);

        return ApplicationResponse.ok(response);
    }

    @GetMapping("/{member-name}")
    public ApplicationResponse<List<GetAccountResponseDto>> getAccount(@PathVariable("member-name") String memberName) {
        List<GetAccountResponseDto> response = accountService.getAccount(memberName);

        return ApplicationResponse.ok(response);
    }
}
