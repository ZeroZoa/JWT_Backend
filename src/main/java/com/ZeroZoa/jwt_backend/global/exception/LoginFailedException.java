package com.ZeroZoa.jwt_backend.global.exception;

// 로그인 실패 (아이디 또는 비밀번호 틀림)
public class LoginFailedException extends BusinessException{
    public LoginFailedException(String message) {
        super(message);
    }
}
