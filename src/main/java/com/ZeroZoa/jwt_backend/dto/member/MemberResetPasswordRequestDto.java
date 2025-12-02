package com.ZeroZoa.jwt_backend.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberResetPasswordRequestDto {
    private String email;
    private String verifiedToken;
    private String newPassword1;
    private String newPassword2;
}
