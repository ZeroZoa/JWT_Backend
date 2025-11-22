package com.ZeroZoa.jwt_backend.global.exception;

public class VerificationCodeExpiredException extends BusinessException {
    public VerificationCodeExpiredException(String message) {
        super(message);
    }
}
