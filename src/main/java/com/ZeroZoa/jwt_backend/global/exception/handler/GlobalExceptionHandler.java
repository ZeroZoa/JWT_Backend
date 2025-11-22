package com.ZeroZoa.jwt_backend.global.exception.handler;

import com.ZeroZoa.jwt_backend.global.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalExceptionHandler {

    //409 Conflict (중복)
    @ExceptionHandler(EmailDuplicateException.class)
    public ResponseEntity<String> handleEmailDuplicate(EmailDuplicateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    //409 Conflict (중복)
    @ExceptionHandler(NicknameDuplicateException.class)
    public ResponseEntity<String> handleNicknameDuplicate(NicknameDuplicateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    //400 Bad Request (잘못된 요청)
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<String> handlePasswordMismatch(PasswordMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    //400 Bad Request 이메일 인증 코드 만료 예외
    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<String> handleVerificationCodeExpired(VerificationCodeExpiredException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    //400 Bad Request 이메일 인증 실패 코드 예외
    @ExceptionHandler(VerificationCodeMismatchException.class)
    public ResponseEntity<String> handleVerificationCodeMismatch(VerificationCodeMismatchException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    //401 Unauthorized (인증 실패 - 아이디/비밀번호 틀림)
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<String> handleLoginFailed(LoginFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    //403 Forbidden (인가 실패 - 이메일 인증 안 됨)
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<String> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    //401 - 인증이 필요한 API에 토큰 없이 접근
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<String> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
