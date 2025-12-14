package com.ZeroZoa.jwt_backend.config.jwt;

import com.ZeroZoa.jwt_backend.domain.member.Role;
import com.ZeroZoa.jwt_backend.global.auth.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Slf4j
@Component
public class JwtTokenProvider {

    //토큰 발근 키, 만료시간
    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    //토큰에 추가할 내용(권한과 아이디)
    private static final String KEY_ID = "uuid";
    private static final String KEY_ROLE = "role";

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String createAccessToken(UUID memberId, String email, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim(KEY_ID, memberId.toString())
                .claim(KEY_ROLE, role.getKey())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(UUID memberId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim(KEY_ID, memberId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        // 액세스 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(KEY_ROLE) == null) {
            throw new JwtException("권한 정보가 없는 토큰입니다.");
        }

        // Claim에서 권한 정보 가져오기
        String roleKey = claims.get(KEY_ROLE).toString();

        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(roleKey));

        String uuidString = claims.get(KEY_ID, String.class);
        UUID memberId = UUID.fromString(uuidString);

        CustomUserDetails principal = new CustomUserDetails(
                memberId,               // UUID (PK)
                claims.getSubject(),    // Email (로그인 ID)
                "",                     // Password (불필요)
                "",                     // Nickname (토큰에 없으므로 빈값)
                authorities
        );

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    //
    private Claims parseClaims(String accessToken) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }

    //토큰 재발급 로직 전용
    public Claims parseExpiredTokenClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이어도 Claims를 반환
            return e.getClaims();
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key) //비밀키 대조
                    .build()
                    .parseSignedClaims(token); //토큰 encoding -> 유효기간 검사
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }
}
