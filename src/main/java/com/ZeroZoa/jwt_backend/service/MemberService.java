package com.ZeroZoa.jwt_backend.service;

import com.ZeroZoa.jwt_backend.domain.member.Member;
import com.ZeroZoa.jwt_backend.dto.member.MemberResetPasswordRequestDto;
import com.ZeroZoa.jwt_backend.dto.member.MemberResponseDto;
import com.ZeroZoa.jwt_backend.dto.member.MemberSignUpRequestDto;
import com.ZeroZoa.jwt_backend.dto.member.MemberSignUpResponseDto;
import com.ZeroZoa.jwt_backend.global.exception.*;
import com.ZeroZoa.jwt_backend.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String VERIFIED_EMAIL_KEY_PREFIX = "verified-email:";

    @Transactional
    public MemberSignUpResponseDto signup(MemberSignUpRequestDto memberSignUpRequestDto){

        String verifiedRedisKey = VERIFIED_EMAIL_KEY_PREFIX + memberSignUpRequestDto.getVerifiedToken();
        String verifiedEmail = redisTemplate.opsForValue().get(verifiedRedisKey);

        if (verifiedEmail == null || !verifiedEmail.equals(memberSignUpRequestDto.getEmail())) {
            throw new IllegalArgumentException("유효하지 않은 이메일 인증 토큰입니다.");
        }

        if(!memberSignUpRequestDto.getPassword1().equals(memberSignUpRequestDto.getPassword2())){
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }

        if(memberRepository.existsByEmail(memberSignUpRequestDto.getEmail())) {
            throw new EmailDuplicateException("이미 가입된 이메일입니다.");
        }

        if(memberRepository.existsByNickname(memberSignUpRequestDto.getNickname())) {
            throw new NicknameDuplicateException("이미 존재하는 닉네임입니다.");
        }

        Member member = Member.createMember(
                memberSignUpRequestDto.getEmail(),
                passwordEncoder.encode(memberSignUpRequestDto.getPassword1()),
                memberSignUpRequestDto.getNickname()
        );

        member.markEmailAsVerified();
        Member savedMember = memberRepository.save(member);

        redisTemplate.delete(verifiedRedisKey);
        return new MemberSignUpResponseDto(savedMember);
    }

    @Transactional
    public void resetPassword(MemberResetPasswordRequestDto memberResetPasswordRequestDto){
        Member member = memberRepository.findByEmail(memberResetPasswordRequestDto.getEmail())
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));

        String redisKey = VERIFIED_EMAIL_KEY_PREFIX + memberResetPasswordRequestDto.getVerifiedToken();
        String verifiedEmail = redisTemplate.opsForValue().get(redisKey);

        if (verifiedEmail == null || !verifiedEmail.equals(memberResetPasswordRequestDto.getEmail())) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 인증 토큰입니다.");
        }

        if (!memberResetPasswordRequestDto.getNewPassword1().equals(memberResetPasswordRequestDto.getNewPassword2())) {
            throw new PasswordMismatchException("새 비밀번호가 서로 일치하지 않습니다.");
        }

        member.updatePassword(passwordEncoder.encode(memberResetPasswordRequestDto.getNewPassword1()));

        String refreshTokenKey = REFRESH_TOKEN_PREFIX + member.getEmail();
        redisTemplate.delete(refreshTokenKey);

        // 6. 사용한 인증 토큰 삭제 (재사용 방지)
        redisTemplate.delete(redisKey);
    }

    public MemberResponseDto getMemberInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        return MemberResponseDto.from(member);
    }

}
