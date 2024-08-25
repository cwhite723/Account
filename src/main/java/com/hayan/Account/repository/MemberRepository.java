package com.hayan.Account.repository;

import com.hayan.Account.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> getByName(String name);
    Long countById(Long memberId);
}
