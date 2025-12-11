package com.ZeroZoa.jwt_backend.config;

import com.ZeroZoa.jwt_backend.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws  Exception{
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함 (JWT)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST,"/api/members/signup").permitAll() //인증 없이 접근 가능한 엔드포인트
                        .requestMatchers(HttpMethod.POST,"/api/auth/login").permitAll() //로그인 API도 허용
                        .requestMatchers(HttpMethod.DELETE,"/api/auth/logout").permitAll() //로그인 API도 허용
                        .requestMatchers(HttpMethod.POST,"/api/auth/reissue").permitAll() //로그인 API도 허용
                        .requestMatchers(HttpMethod.PUT,"/api/members/reset-password").permitAll() //로그인 API도 허용
                        .requestMatchers(HttpMethod.POST,"/api/email/send-signup-verification-code").permitAll() //이메일 인증 코드 발송 API 허용
                        .requestMatchers(HttpMethod.POST,"/api/email/send-password-reset-verification-code").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/email/check-verification-code").permitAll() //로그인 API도 허용

                        .requestMatchers(HttpMethod.GET, "/api/members/myinfo").authenticated()

                        .anyRequest().permitAll() //개발용
                        //.anyRequest().authenticated() //배포용
                        // 나머지 요청은 일단 모두 허용 (나중에 '인증' 필요로 변경, 개발용 배포시 .authenticated()로 변경)


                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization")); //프론트에서 JWT 토큰 접근 허용
        configuration.setAllowCredentials(true); //쿠키 기반 인증을 위한 설정

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
