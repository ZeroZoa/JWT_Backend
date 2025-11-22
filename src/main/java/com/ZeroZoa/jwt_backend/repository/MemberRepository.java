package com.ZeroZoa.jwt_backend.repository;

import com.ZeroZoa.jwt_backend.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    //@return email을 통해 Member를 찾으면 Optional<Member> 반환, 못 찾으면 Optional.empty() 반환
    Optional<Member> findByEmail(String email);

    //@return email을 통해 Member를 찾으면 true 반환, 못 찾으면 false 반환
    boolean existsByEmail(String email);

    //@return nickname을 통해 Member를 찾으면 true 반환, 못 찾으면 false 반환
    boolean existsByNickname(String nickname);
}
