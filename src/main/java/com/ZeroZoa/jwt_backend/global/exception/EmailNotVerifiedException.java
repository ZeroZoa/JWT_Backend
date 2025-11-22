package com.ZeroZoa.jwt_backend.global.exception;

// 이메일 미인증 계정 알림
public class EmailNotVerifiedException extends BusinessException{
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
