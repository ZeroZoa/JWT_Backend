package com.ZeroZoa.jwt_backend.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberLogInRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

}
