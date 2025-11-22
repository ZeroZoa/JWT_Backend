package com.ZeroZoa.jwt_backend.global.exception;

public class PasswordMismatchException extends BusinessException{

    public PasswordMismatchException(String message) {
        super(message);
    }

}
