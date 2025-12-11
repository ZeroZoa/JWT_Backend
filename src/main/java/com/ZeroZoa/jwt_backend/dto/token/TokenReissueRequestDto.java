package com.ZeroZoa.jwt_backend.dto.token;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueRequestDto {
    private String accessToken;
    private String refreshToken;
}
