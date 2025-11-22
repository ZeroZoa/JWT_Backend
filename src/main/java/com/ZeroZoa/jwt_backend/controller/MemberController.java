package com.ZeroZoa.jwt_backend.controller;

import com.ZeroZoa.jwt_backend.dto.member.MemberLogInRequestDto;
import com.ZeroZoa.jwt_backend.dto.member.MemberSignUpRequestDto;
import com.ZeroZoa.jwt_backend.dto.member.MemberSignUpResponseDto;
import com.ZeroZoa.jwt_backend.dto.token.CreateTokenResponseDto;
import com.ZeroZoa.jwt_backend.global.exception.AuthenticationFailedException;
import com.ZeroZoa.jwt_backend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<MemberSignUpResponseDto> signUp(
            @Valid @RequestBody MemberSignUpRequestDto memberSignUpRequestDto
    ) {
        MemberSignUpResponseDto memberSignUpResponseDto = memberService.signUp(memberSignUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberSignUpResponseDto);
    }


    @PostMapping("/login")
    public ResponseEntity<CreateTokenResponseDto> login(
            @Valid @RequestBody MemberLogInRequestDto memberLogInRequestDto
    ) {
        CreateTokenResponseDto createTokenResponseDto = memberService.logIn(memberLogInRequestDto);
        return ResponseEntity.ok(createTokenResponseDto);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> logout() {
        //SecurityContext에서 현재 인증된 사용자의 이메일 가져오기
        String email = getEmailFromSecurityContext();

        //서비스 호출 (Redis의 Refresh Token 삭제)
        memberService.logOut(email);

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
