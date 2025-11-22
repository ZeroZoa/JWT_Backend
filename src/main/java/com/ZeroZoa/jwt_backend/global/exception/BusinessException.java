package com.ZeroZoa.jwt_backend.global.exception;

public class BusinessException extends RuntimeException{

    public BusinessException(String message){
        super(message);
    }

}
