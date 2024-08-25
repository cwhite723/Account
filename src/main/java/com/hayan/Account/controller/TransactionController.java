package com.hayan.Account.controller;

import com.hayan.Account.common.ApplicationResponse;
import com.hayan.Account.domain.dto.request.CancelUseRequestDto;
import com.hayan.Account.domain.dto.request.UseBalanceRequestDto;
import com.hayan.Account.domain.dto.response.TransactionResponseDto;
import com.hayan.Account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/use-balance")
    public ApplicationResponse<TransactionResponseDto> useBalance(@Valid @RequestBody UseBalanceRequestDto request) {
        TransactionResponseDto response = transactionService.useBalance(request);

        return ApplicationResponse.ok(response);
    }

    @PostMapping("/cancel-use")
    public ApplicationResponse<TransactionResponseDto> cancelUse(@Valid @RequestBody CancelUseRequestDto request) {
        TransactionResponseDto response = transactionService.cancelUse(request);

        return ApplicationResponse.ok(response);
    }

    @GetMapping("/{transaction-id}")
    public ApplicationResponse<TransactionResponseDto> getTransaction(@PathVariable("transaction-id") Long transactionId) {
        TransactionResponseDto response = transactionService.getTransaction(transactionId);

        return ApplicationResponse.ok(response);
    }
}
