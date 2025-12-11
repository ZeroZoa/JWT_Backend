package com.ZeroZoa.jwt_backend.global.auth;

import com.ZeroZoa.jwt_backend.domain.member.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {
    private final UUID id;
    private final String email;

    @JsonIgnore
    private final String password;
    private final String nickname;

    // Spring Security가 사용할 권한 목록
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UUID id, String email, String password, String nickname, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.authorities = authorities;
    }

    public static CustomUserDetails from(Member member) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(member.getRole().getKey())
        );

        return new CustomUserDetails(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getNickname(),
                authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // 계정 만료 여부 (true: 만료 안됨)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부 (true: 만료 안됨)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 (true: 활성화)
    @Override
    public boolean isEnabled() {
        return true;
    }
}
