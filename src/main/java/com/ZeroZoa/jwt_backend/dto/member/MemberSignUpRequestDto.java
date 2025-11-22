package com.ZeroZoa.jwt_backend.dto.member;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class MemberSignUpRequestDto {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password1;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String password2;

    @NotEmpty(message = "닉네임을 입력해주세요.")
    @Length(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 알파벳, 숫자만 사용할 수 있습니다.")
    private String nickname;

    private String verifiedToken;
}
