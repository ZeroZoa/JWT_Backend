package com.ZeroZoa.jwt_backend.domain.member;

import com.ZeroZoa.jwt_backend.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "member_id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(name = "email_verified_at", nullable = true)
    private Instant emailVerifiedAt = null;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Member(String email, String password, String nickname, Role role){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public static Member createMember(String email, String password, String nickname) {
        return new Member(
                email,
                password,
                nickname,
                Role.USER
        );
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void markEmailAsVerified() {
        this.emailVerifiedAt = Instant.now();
    }
}
