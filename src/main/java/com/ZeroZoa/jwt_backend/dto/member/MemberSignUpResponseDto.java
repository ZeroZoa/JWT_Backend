package com.ZeroZoa.jwt_backend.dto.member;

import com.ZeroZoa.jwt_backend.domain.member.Member;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MemberSignUpResponseDto {

    private UUID id;
    private String email;
    private String nickname;

    public MemberSignUpResponseDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
    }
}
