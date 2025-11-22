package com.ZeroZoa.jwt_backend.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        //Request Header에서 토큰 추출
        String jwt = resolveToken(request);

        //validateToken으로 토큰 유효성 검사
        // (StringUtils.hasText(jwt): jwt가 null이 아니면서, 공백이 아닌지 확인)
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            //토큰이 유효할 경우, 토큰에서 Authentication 객체를 가져와서 SecurityContext에 저장
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        //다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7); // "Bearer " (7글자) 이후의 토큰 반환
        }
        return null;
    }
}
