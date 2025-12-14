package com.ZeroZoa.jwt_backend.service;

import com.ZeroZoa.jwt_backend.config.jwt.JwtTokenProvider;
import com.ZeroZoa.jwt_backend.domain.member.Member;
import com.ZeroZoa.jwt_backend.dto.member.MemberLoginRequestDto;
import com.ZeroZoa.jwt_backend.dto.token.CreateTokenResponseDto;
import com.ZeroZoa.jwt_backend.dto.token.TokenDto;
import com.ZeroZoa.jwt_backend.dto.token.TokenReissueRequestDto;
import com.ZeroZoa.jwt_backend.global.exception.EmailNotVerifiedException;
import com.ZeroZoa.jwt_backend.global.exception.LoginFailedException;
import com.ZeroZoa.jwt_backend.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public CreateTokenResponseDto login(MemberLoginRequestDto memberLoginRequestDto){

        log.info("로그인 시도 - email: {}", memberLoginRequestDto.getEmail());
        Member member = memberRepository.findByEmail(memberLoginRequestDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 (존재하지 않는 사용자) - email: {}", memberLoginRequestDto.getEmail());
                    return new LoginFailedException("로그인 정보가 올바르지 않습니다.");
                });

        if (!passwordEncoder.matches(memberLoginRequestDto.getPassword(), member.getPassword())) {
            log.warn("로그인 실패 (비밀번호 불일치) - email: {}", member.getEmail());
            throw new LoginFailedException("로그인 정보가 올바르지 않습니다.");
        }

        if (member.getEmailVerifiedAt() == null) {
            log.warn("로그인 실패 (이메일 미인증) - email: {}", member.getEmail());
            throw new EmailNotVerifiedException("이메일 인증이 완료되지 않은 계정입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getEmail());

        String redisKey = REFRESH_TOKEN_PREFIX + member.getEmail();
        redisTemplate.opsForValue().set(
                redisKey,
                refreshToken,
                refreshTokenExpirationMs,
                TimeUnit.MILLISECONDS
        );

        log.info("로그인 성공 - email: {}", member.getEmail());

        return CreateTokenResponseDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenDto reissue(TokenReissueRequestDto tokenReissueRequestDto) {

        if (!jwtTokenProvider.validateToken(tokenReissueRequestDto.getRefreshToken())) {
            log.warn("토큰 재발급 실패 (유효하지 않은 Refresh Token)");
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        //Access Token에서 Member 이메일 가져오기
        String email = jwtTokenProvider.getSubject(tokenReissueRequestDto.getRefreshToken());

        log.info("토큰 재발급 요청 - email: {}", email);

        //Redis에서 저장된 Refresh Token 가져오기
        String redisKey = REFRESH_TOKEN_PREFIX + email;
        String savedRefreshToken = redisTemplate.opsForValue().get(redisKey);

        //Redis 검증 (토큰이 없거나 일치하지 않으면 예외)
        if (savedRefreshToken == null || !savedRefreshToken.equals(tokenReissueRequestDto.getRefreshToken())) {
            log.warn("토큰 불일치 발생. 사용자: {}", email);
            throw new RuntimeException("Refresh Token 정보가 일치하지 않습니다.");
        }

        //Member 정보 조회 (Role 정보 획득 목적)
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("토큰 재발급 실패 (DB 사용자 없음) - email: {}", email);
                    return new EntityNotFoundException("회원을 찾을 수 없습니다.");
                });

        //토큰 각각 생성 (로그인 로직과 동일하게 변경)
        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getEmail());

        //Redis 업데이트 (기존 키에 덮어쓰기)
        redisTemplate.opsForValue().set(
                redisKey,
                newRefreshToken,
                refreshTokenExpirationMs,
                TimeUnit.MILLISECONDS
        );

        log.info("토큰 재발급 성공 - email: {}", email);

        //응답 반환
        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


    @Transactional
    public void logout(String email) {
        log.info("로그아웃 요청 - email: {}", email);
        String redisKey = REFRESH_TOKEN_PREFIX + email;

        String refreshTokenInRedis = redisTemplate.opsForValue().get(redisKey);

        if (refreshTokenInRedis != null) {
            redisTemplate.delete(redisKey);
            log.info("로그아웃 완료 (Redis 토큰 삭제됨) - email: {}", email);
        } else{
            log.warn("로그아웃 요청했으나 Redis에 토큰 없음 (이미 로그아웃/만료) - email: {}", email);
        }
    }
}
