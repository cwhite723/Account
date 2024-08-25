package com.hayan.Account.domain.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequestDto(
        @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
        String memberName,

        @Min(message = "초기 금액은 0보다 커야 합니다.", value = 0)
        Integer balance
) { }
