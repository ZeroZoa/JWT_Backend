package com.ZeroZoa.jwt_backend.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberLoginRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

}
