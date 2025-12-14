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
        log.info("회원가입 요청 진입 - email: {}, nickname: {}", memberSignUpRequestDto.getEmail(), memberSignUpRequestDto.getNickname());

        String verifiedRedisKey = VERIFIED_EMAIL_KEY_PREFIX + memberSignUpRequestDto.getVerifiedToken();
        String verifiedEmail = redisTemplate.opsForValue().get(verifiedRedisKey);

        if (verifiedEmail == null || !verifiedEmail.equals(memberSignUpRequestDto.getEmail())) {
            log.warn("회원가입 실패 (이메일 인증 토큰 유효하지 않음) - email: {}", memberSignUpRequestDto.getEmail());
            throw new IllegalArgumentException("유효하지 않은 이메일 인증 토큰입니다.");
        }

        if(!memberSignUpRequestDto.getPassword1().equals(memberSignUpRequestDto.getPassword2())){
            log.warn("회원가입 실패 (비밀번호 불일치) - email: {}", memberSignUpRequestDto.getEmail());
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }

        if(memberRepository.existsByEmail(memberSignUpRequestDto.getEmail())) {
            log.warn("회원가입 실패 (이미 존재하는 이메일) - email: {}", memberSignUpRequestDto.getEmail());
            throw new EmailDuplicateException("이미 가입된 이메일입니다.");
        }

        if(memberRepository.existsByNickname(memberSignUpRequestDto.getNickname())) {
            log.warn("회원가입 실패 (이미 존재하는 닉네임) - nickname: {}", memberSignUpRequestDto.getNickname());
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

        log.info("회원가입 성공 - memberId: {}, email: {}", savedMember.getId(), savedMember.getEmail());
        return new MemberSignUpResponseDto(savedMember);
    }

    @Transactional
    public void resetPassword(MemberResetPasswordRequestDto memberResetPasswordRequestDto){
        log.info("비밀번호 재설정 요청 - email: {}", memberResetPasswordRequestDto.getEmail());

        Member member = memberRepository.findByEmail(memberResetPasswordRequestDto.getEmail())
                .orElseThrow(() ->{
                    log.warn("비밀번호 재설정 실패 (존재하지 않는 회원) - email: {}", memberResetPasswordRequestDto.getEmail());
                    return new MemberNotFoundException("존재하지 않는 회원입니다.");
                });

        String redisKey = VERIFIED_EMAIL_KEY_PREFIX + memberResetPasswordRequestDto.getVerifiedToken();
        String verifiedEmail = redisTemplate.opsForValue().get(redisKey);

        if (verifiedEmail == null || !verifiedEmail.equals(memberResetPasswordRequestDto.getEmail())) {
            log.warn("비밀번호 재설정 실패 (인증 토큰 만료/불일치) - email: {}", memberResetPasswordRequestDto.getEmail());
            throw new IllegalArgumentException("유효하지 않거나 만료된 인증 토큰입니다.");
        }

        if (!memberResetPasswordRequestDto.getNewPassword1().equals(memberResetPasswordRequestDto.getNewPassword2())) {
            log.warn("비밀번호 재설정 실패 (새 비밀번호 불일치) - email: {}", memberResetPasswordRequestDto.getEmail());
            throw new PasswordMismatchException("새 비밀번호가 서로 일치하지 않습니다.");
        }

        member.updatePassword(passwordEncoder.encode(memberResetPasswordRequestDto.getNewPassword1()));

        String refreshTokenKey = REFRESH_TOKEN_PREFIX + member.getEmail();
        redisTemplate.delete(refreshTokenKey);

        //사용한 인증 토큰 삭제 (재사용 방지)
        redisTemplate.delete(redisKey);

        log.info("비밀번호 변경 완료 및 전체 로그아웃 처리 - memberId: {}, email: {}", member.getId(), member.getEmail());
    }

    public MemberResponseDto getMemberInfo(String email) {
        log.info("회원 정보 조회 요청 - email: {}", email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("회원 정보 조회 실패 (DB 데이터 없음) - email: {}", email);
                    return new EntityNotFoundException("회원을 찾을 수 없습니다.");
                });

        return MemberResponseDto.from(member);
    }
}
