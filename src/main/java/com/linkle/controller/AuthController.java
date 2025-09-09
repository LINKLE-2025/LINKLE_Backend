package com.linkle.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkle.domain.dto.ExistsResponseDTO;
import com.linkle.domain.dto.UserAuthDTO;
import com.linkle.domain.dto.UserLoginRequestDTO;
import com.linkle.domain.dto.UserLoginResponseDTO;
import com.linkle.domain.dto.UserSignupRequestDTO;
import com.linkle.service.AuthService;
import com.linkle.service.AuthMailService;
import com.linkle.util.CodeGenerator;
import com.linkle.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // 서비스 의존성 주입
    private final AuthService authService;
    private final AuthMailService authMailService;
    private final JwtTokenProvider jwtTokenProvider;

    // JWT 토큰 만료 시간
    @Value("${jwt.access-token-expiration:900000}")
    private long accessExpiration;
    @Value("${jwt.refresh-token-expiration:1209600000}")
    private long refreshExpiration;


    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDTO requestDTO) {
        System.out.println("받은 로그인 요청: " + requestDTO);
        try {
            // 로그인 처리 및 JWT 토큰 발급
            UserLoginResponseDTO responseDTO = authService.login(requestDTO);
            System.out.println("발급된 토큰: " + responseDTO);
            // ✅ 토큰을 HttpOnly Cookie로 설정
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", responseDTO.getAccessToken())
                .httpOnly(true)         // JS 접근 차단
                .secure(true)           // HTTPS 환경에서만 전송
                .sameSite("None")     // CSRF 방지
                .path("/")              // 모든 경로에서 전송
                .maxAge(Duration.ofMillis(accessExpiration)) // 만료 시간
                .build();
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", responseDTO.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(refreshExpiration))
                .build();
            // 로그인 성공 시 토큰을 포함한 응답 반환
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("success", true, "message", "로그인 성공"));
        } catch (RuntimeException e) {
            // 로그인 실패 시 에러 메시지 반환
            System.out.println("로그인 실패: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            // 기타 예외 처리
            System.out.println("로그인 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // 로그인한 사용자정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@CookieValue(value = "accessToken", required = false) String accessToken ) {
        // 쿠키에 토큰이 없으면 401 반환
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "토큰이 없습니다."));
        }
        try {
            // 토큰 검증
            if (!jwtTokenProvider.validateToken(accessToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "유효하지 않은 토큰"));
            }
            // 토큰에서 사용자 ID 추출
            String sub = jwtTokenProvider.getUserIdFromToken(accessToken);
            Long userId = Long.parseLong(sub);
            UserAuthDTO dto = authService.getCurrentUser(userId);
            return ResponseEntity.ok(dto);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "토큰이 만료되었습니다."));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "토큰의 subject가 잘못되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "인증 실패"));
        }
    }

    // Refresh Token으로 Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
        @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "리프레시 토큰이 없습니다."));
        }

        try {
            // refreshToken 에서 userId 추출
            String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

            // Access Token 재발급
            String newAccessToken = authService.refreshAccessToken(Long.parseLong(userId), refreshToken);

            // ✅ 새 Access Token을 HttpOnly Cookie로 설정
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(accessExpiration)) // 만료 시간 동일하게
                .build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(Map.of("success", true, "message", "Access Token 재발급 성공"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
        }
    }


    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 쿠키 삭제를 위한 빈 쿠키 생성
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(0) // 즉시 만료
            .build();
        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(0) // 즉시 만료
            .build();
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
            .body(Map.of("success", true, "message", "로그아웃 성공"));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Boolean> signup(@RequestBody UserSignupRequestDTO requestDTO) {
        System.out.println("받은 회원가입 요청: " + requestDTO);
        boolean signupSuccess = authService.signup(requestDTO);  // 회원가입 처리
        return ResponseEntity.ok(signupSuccess); // 성공 여부 반환
    }

    // 이메일 중복 체크
    @GetMapping("/email/{email}")
    public ExistsResponseDTO emailCheck(@PathVariable String email) {
        boolean emailExists = authService.isEmailExists(email);
        return new ExistsResponseDTO(emailExists);
    }

    // 닉네임 중복 체크
    @GetMapping("/nickname/{nickname}")
    public ExistsResponseDTO nicknameCheck(@PathVariable String nickname) {
        boolean nicknameExists = authService.isNicknameExists(nickname);
        return new ExistsResponseDTO(nicknameExists);
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
