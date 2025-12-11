package com.ZeroZoa.jwt_backend.dto.member;

import com.ZeroZoa.jwt_backend.domain.member.Member;
import com.ZeroZoa.jwt_backend.domain.member.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDto {
    private UUID id;
    private String email;
    private String nickname;
    private Role role;

    // Entity -> DTO 변환 편의 메서드
    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole())
                .build();
    }
}
