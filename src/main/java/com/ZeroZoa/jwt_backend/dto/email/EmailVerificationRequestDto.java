package com.ZeroZoa.jwt_backend.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerificationRequestDto {

    @Email
    @NotBlank
    private String email;
}
