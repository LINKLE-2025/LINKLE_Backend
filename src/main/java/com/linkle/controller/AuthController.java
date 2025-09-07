package com.linkle.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkle.domain.dto.DuplicationCheckResponseDTO;
import com.linkle.service.AuthService;
import com.linkle.service.AuthMailService;
import com.linkle.util.CodeGenerator;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthMailService authMailService;

    // 로그인
    @PostMapping("/login")
    public String login() {
        return "login";
    }

    // 회원가입
    @PostMapping("/signup")
    public String signup() {
        return "signup";
    }

    // 이메일 중복 체크
    @GetMapping("/auth/email/{email}")
    public DuplicationCheckResponseDTO emailCheck(@PathVariable String email) {
        boolean emailExists = authService.isEmailExists(email);
        return new DuplicationCheckResponseDTO(emailExists);
    }

    // 닉네임 중복 체크
    @GetMapping("/auth/nickname/{nickname}")
    public DuplicationCheckResponseDTO nicknameCheck(@PathVariable String nickname) {
        boolean nicknameExists = authService.isNicknameExists(nickname);
        return new DuplicationCheckResponseDTO(nicknameExists);
    }

    // 이메일 인증 코드 발송
    @PostMapping("/auth/email/{email:.+}")
    public ResponseEntity<Void> sendCode(@PathVariable String email) {
        String code = CodeGenerator.generateCode();  // 6자리 인증 코드 생성
        System.out.println("Verification code for " + email + ": " + code);
        authMailService.saveCode(email, code);  // 코드 저장
        authMailService.sendMail(email, "LINKLE 이메일 인증번호", "인증번호: " + code);  // 이메일 발송
        return ResponseEntity.ok().build();
    }

    // 이메일 인증 코드 검증
    @PostMapping("/auth/email/{email:.+}/code/{code}")
    public ResponseEntity<Boolean> verifyEmail(@PathVariable String email, @PathVariable String code) {
        boolean verified = authMailService.verifyCode(email, code);  // 코드 검증
        System.out.println("Verification result for " + email + ": " + verified);
        return ResponseEntity.ok(verified);
    }

}
