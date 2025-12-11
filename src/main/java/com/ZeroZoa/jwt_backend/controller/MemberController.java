package com.ZeroZoa.jwt_backend.controller;

import com.ZeroZoa.jwt_backend.domain.member.Role;
import com.ZeroZoa.jwt_backend.dto.member.*;
import com.ZeroZoa.jwt_backend.global.auth.CustomUserDetails;
import com.ZeroZoa.jwt_backend.global.exception.AuthenticationFailedException;
import com.ZeroZoa.jwt_backend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<MemberSignUpResponseDto> signup(
            @Valid @RequestBody MemberSignUpRequestDto memberSignUpRequestDto
    ) {
        MemberSignUpResponseDto memberSignUpResponseDto = memberService.signup(memberSignUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberSignUpResponseDto);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody MemberResetPasswordRequestDto memberResetPasswordRequestDto) {
        memberService.resetPassword(memberResetPasswordRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // [추가된 부분] 인증 정보가 없는 경우(토큰 만료/오류) 401 에러 반환
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 유효하지 않습니다.");
        }

        // [기존 로직] 이제 customUserDetails가 null이 아님이 보장되므로 안전합니다.
        Role userRole = customUserDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority) // "ROLE_USER"
                .map(str -> str.replace("ROLE_", "")) // "USER"
                .map(Role::valueOf)                   // Role.USER
                .orElseThrow(() -> new IllegalStateException("권한 정보를 찾을 수 없습니다."));

        // DTO 생성
        MemberResponseDto response = MemberResponseDto.builder()
                .id(customUserDetails.getId())
                .email(customUserDetails.getUsername())
                .nickname(customUserDetails.getNickname())
                .role(userRole)
                .build();

        return ResponseEntity.ok(response);
    }

    private String getEmailFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            throw new AuthenticationFailedException("인증 정보가 없습니다. 로그인이 필요합니다.");
        }

        return authentication.getName();
    }
}
