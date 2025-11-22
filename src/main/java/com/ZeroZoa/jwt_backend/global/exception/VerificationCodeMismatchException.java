package com.ZeroZoa.jwt_backend.global.exception;

public class VerificationCodeMismatchException extends BusinessException {
    public VerificationCodeMismatchException(String message) {
        super(message);
    }
}
