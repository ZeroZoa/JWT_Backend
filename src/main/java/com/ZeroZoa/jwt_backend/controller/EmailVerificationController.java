package com.ZeroZoa.jwt_backend.controller;

import com.ZeroZoa.jwt_backend.dto.email.EmailVerificationCheckRequestDto;
import com.ZeroZoa.jwt_backend.dto.email.EmailVerificationRequestDto;
import com.ZeroZoa.jwt_backend.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(
            @Valid @RequestBody EmailVerificationRequestDto emailVerificationRequestDto
    ) {
        emailVerificationService.sendVerificationCode(emailVerificationRequestDto.getEmail());

        return ResponseEntity.ok("인증 코드가 전송되었습니다.");
    }

    @PostMapping("/check-verification-code")
    public ResponseEntity<?> checkVerificationCode(
            @Valid @RequestBody EmailVerificationCheckRequestDto emailVerificationCheckRequestDto
    ) {

        String verifiedToken = emailVerificationService.checkVerificationCode(emailVerificationCheckRequestDto.getEmail(),
                emailVerificationCheckRequestDto.getCode());

        if (verifiedToken == null) {
            return ResponseEntity.badRequest().body("인증 코드가 유효하지 않거나 만료되었습니다.");
        }

        Map<String, String> response = new HashMap<>();
        response.put("verifiedToken", verifiedToken);
        return ResponseEntity.ok(response);
    }

}
