package com.ZeroZoa.jwt_backend.global.exception;

public class AuthenticationFailedException extends BusinessException{
    public AuthenticationFailedException(String message){
        super(message);
    }
}
