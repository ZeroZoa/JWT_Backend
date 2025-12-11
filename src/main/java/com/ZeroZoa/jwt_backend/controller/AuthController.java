package com.ZeroZoa.jwt_backend.controller;

import com.ZeroZoa.jwt_backend.dto.member.MemberLoginRequestDto;
import com.ZeroZoa.jwt_backend.dto.token.CreateTokenResponseDto;
import com.ZeroZoa.jwt_backend.dto.token.TokenDto;
import com.ZeroZoa.jwt_backend.dto.token.TokenReissueRequestDto;
import com.ZeroZoa.jwt_backend.global.exception.AuthenticationFailedException;
import com.ZeroZoa.jwt_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<CreateTokenResponseDto> login(
            @Valid @RequestBody MemberLoginRequestDto memberLoginRequestDto
    ) {
        CreateTokenResponseDto createTokenResponseDto = authService.login(memberLoginRequestDto);
        return ResponseEntity.ok(createTokenResponseDto);
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenReissueRequestDto tokenReissueRequestDto) {
        return ResponseEntity.ok(authService.reissue(tokenReissueRequestDto));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> logout() {
        String email = getEmailFromSecurityContext();

        authService.logout(email);

        return ResponseEntity.ok("로그아웃되었습니다.");
    }

    private String getEmailFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            throw new AuthenticationFailedException("인증 정보가 없습니다. 로그인이 필요합니다.");
        }

        return authentication.getName();
    }
}
