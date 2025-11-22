package com.ZeroZoa.jwt_backend.dto.token;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateTokenResponseDto {
    private final String grantType; // (실무) "Bearer"
    private final String accessToken;
    private final String refreshToken;

    @Builder
    public CreateTokenResponseDto(String grantType, String accessToken, String refreshToken) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
