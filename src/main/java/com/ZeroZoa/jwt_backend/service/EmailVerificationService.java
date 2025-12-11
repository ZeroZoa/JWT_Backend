package com.ZeroZoa.jwt_backend.service;

import com.ZeroZoa.jwt_backend.global.exception.EmailDuplicateException;
import com.ZeroZoa.jwt_backend.global.exception.VerificationCodeExpiredException;
import com.ZeroZoa.jwt_backend.global.exception.VerificationCodeMismatchException;
import com.ZeroZoa.jwt_backend.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    //Redis에 저장하기위한 태그
    private static final String EMAIL_VERIFICATION_KEY_PREFIX = "email-verification:"; //인증 받을 메일
    private static final String VERIFIED_EMAIL_KEY_PREFIX = "verified-email:"; //인증 완료 메일
    //Redis에 저장된 값의 만료 시간 (분 단위)
    private static final long VERIFICATION_CODE_EXPIRATION_MINUTES = 5;

    //6자리 인증번호 생성
    public String createVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    @Transactional
    public void sendSignUpVerificationCode(String email) {
        // [수정] 회원가입 시에는 이미 존재하는 이메일이면 예외 발생
        if (memberRepository.existsByEmail(email)) {
            throw new EmailDuplicateException("이미 가입된 이메일입니다.");
        }

        // [수정] 공통 로직 호출
        sendVerificationCodeInternal(email);
    }

    /**
     * 비밀번호 재설정용 인증 코드 발송
     * - 가입된 회원인지 확인 필수
     */
    @Transactional
    public void sendPasswordResetVerificationCode(String email) {
        // [수정] 비밀번호 재설정 시에는 회원이 존재하지 않으면 예외 발생
        if (!memberRepository.existsByEmail(email)) {
            throw new EntityNotFoundException("가입되지 않은 이메일입니다.");
        }

        // [수정] 공통 로직 호출
        sendVerificationCodeInternal(email);
    }

    /**
     * 실제 인증 코드를 생성하고 메일을 보내는 내부 로직
     * 외부에서 직접 호출할 수 없도록 private으로 선언
     */
    private void sendVerificationCodeInternal(String email) {
        //인증 코드 생성
        String verificationCode = createVerificationCode();

        //인증 코드 Redis 서버에 저장
        String redisKey = EMAIL_VERIFICATION_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(
                redisKey,
                verificationCode,
                VERIFICATION_CODE_EXPIRATION_MINUTES,
                TimeUnit.MINUTES
        );

        //인증 코드 전송
        try {
            MimeMessage message = createVerificationMessage(email, verificationCode);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("메일 발송 실패: {}", email, e);
            throw new RuntimeException("메일 발송에 실패했습니다.", e);
        }
    }

    @Transactional
    public String checkVerificationCode(String email, String userSubmittedCode){

        //Redis에 저장된 인증코드를 찾기 위한 키
        String redisKey = EMAIL_VERIFICATION_KEY_PREFIX + email;
        //Redis에서 키를 통해 인증코드를 저장
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if(storedCode == null){
            log.warn("인증 실패: 이메일 {}에 대한 코드가 Redis에서 발견되지 않음 (만료 또는 오타)", email);
            throw new VerificationCodeExpiredException("인증 코드가 만료되었거나, 인증 요청을 하지 않은 이메일입니다.");
        }

        if (!storedCode.equals(userSubmittedCode)) {
            log.warn("인증 실패: 이메일 {}의 코드가 일치하지 않습니다.", email);
            throw new VerificationCodeMismatchException("인증 코드가 일치하지 않습니다.");
        }

        // 인증 성공 시, 재사용 방지를 위해 Redis에서 해당 키 삭제
        redisTemplate.delete(redisKey);

        log.info("인증 성공: 이메일 {}", email);
        String verifiedToken = UUID.randomUUID().toString();
        String verifiedRedisKey = VERIFIED_EMAIL_KEY_PREFIX + verifiedToken;
        redisTemplate.opsForValue().set(
                verifiedRedisKey,
                email,
                VERIFICATION_CODE_EXPIRATION_MINUTES,
                TimeUnit.MINUTES);

        return verifiedToken;
    }

    //인증 코드을 포맷에 담아 포장
    public MimeMessage createVerificationMessage(String email, String verificationCode) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(email);
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("[JWT] 이메일 인증 코드 안내");
        String body = "<h3>[JWT] 요청하신 인증 코드입니다.</h3>"
                + "<h1>" + verificationCode + "</h1>"
                + "<h3>코드를 입력하여 인증을 완료해주세요.</h3>";
        message.setText(body, "UTF-8", "html");

        return message;
    }

}
