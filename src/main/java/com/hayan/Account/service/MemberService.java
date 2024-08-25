package com.hayan.Account.service;

import com.hayan.Account.domain.Member;
import com.hayan.Account.exception.CustomException;
import com.hayan.Account.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.hayan.Account.exception.ErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    Member getByName(String name) {
        return memberRepository.getByName(name)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }
}
