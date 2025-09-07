package com.linkle.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkle.domain.dto.DuplicationCheckResponseDTO;
import com.linkle.domain.dto.UserLoginRequestDTO;
import com.linkle.domain.dto.UserSignupRequestDTO;
import com.linkle.service.AuthService;
import com.linkle.service.AuthMailService;
import com.linkle.util.CodeGenerator;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthMailService authMailService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        System.out.println("받은 로그인 요청: " + userLoginRequestDTO);
        try {
            // 로그인 처리 및 JWT 토큰 발급
            String token = authService.login(userLoginRequestDTO);
            System.out.println("발급된 토큰: " + token);
            // 성공 시 토큰과 성공 메시지 반환
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token
            ));
        } catch (RuntimeException e) {
            // 로그인 실패 시 에러 메시지 반환
            System.out.println("로그인 실패: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            // 로그인 실패 시 에러 메시지 반환
            System.out.println("로그인 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Boolean> signup(@RequestBody UserSignupRequestDTO userSignupRequestDTO) {
        System.out.println("받은 회원가입 요청: " + userSignupRequestDTO);
        boolean signupSuccess = authService.signup(userSignupRequestDTO);  // 회원가입 처리
        return ResponseEntity.ok(signupSuccess); // 성공 여부 반환
    }

    // 이메일 중복 체크
    @GetMapping("/email/{email}")
    public DuplicationCheckResponseDTO emailCheck(@PathVariable String email) {
        boolean emailExists = authService.isEmailExists(email);
        return new DuplicationCheckResponseDTO(emailExists);
    }

    // 닉네임 중복 체크
    @GetMapping("/nickname/{nickname}")
    public DuplicationCheckResponseDTO nicknameCheck(@PathVariable String nickname) {
        boolean nicknameExists = authService.isNicknameExists(nickname);
        return new DuplicationCheckResponseDTO(nicknameExists);
    }

    // 이메일 인증 코드 발송
    @PostMapping("/email/{email:.+}")
    public ResponseEntity<Void> sendCode(@PathVariable String email) {
        String code = CodeGenerator.generateCode();  // 6자리 인증 코드 생성
        System.out.println("Verification code for " + email + ": " + code);
        authMailService.saveCode(email, code);  // 코드 저장
        authMailService.sendMail(email, "LINKLE 이메일 인증번호", "인증번호: " + code);  // 이메일 발송
        return ResponseEntity.ok().build();
    }

    // 이메일 인증 코드 검증
    @PostMapping("/email/{email:.+}/code/{code}")
    public ResponseEntity<Boolean> verifyEmail(@PathVariable String email, @PathVariable String code) {
        boolean verified = authMailService.verifyCode(email, code);  // 코드 검증
        System.out.println("Verification result for " + email + ": " + verified);
        return ResponseEntity.ok(verified);
    }

}
