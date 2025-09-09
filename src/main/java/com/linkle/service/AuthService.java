package com.linkle.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.linkle.domain.dto.UserAuthDTO;
import com.linkle.domain.dto.UserLoginRequestDTO;
import com.linkle.domain.dto.UserLoginResponseDTO;
import com.linkle.domain.dto.UserSignupRequestDTO;
import com.linkle.domain.entity.User;
import com.linkle.repository.RefreshTokenRepository;
import com.linkle.repository.UserRepository;
import com.linkle.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 로그인 처리 및 JWT 토큰 발급
    public UserLoginResponseDTO login(UserLoginRequestDTO requestDTO) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(requestDTO.getEmail())
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        System.out.println("입력 비밀번호: " + requestDTO.getPassword());
        System.out.println("저장된 해시: " + user.getPassword());
        System.out.println("매치 결과: " + passwordEncoder.matches(requestDTO.getPassword(), user.getPassword()));

        // 비밀번호 검증
        if (!passwordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId().toString());

        // Redis에 Refresh Token 저장
        refreshTokenRepository.save(user.getUserId().toString(), refreshToken);

        return new UserLoginResponseDTO(accessToken, refreshToken);
    }

    // 로그인한 사용자 정보 조회
    public UserAuthDTO getCurrentUser(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // UserAuthDTO로 변환하여 반환
        return UserAuthDTO.builder()
                    .userId(user.getUserId())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .age(user.getAge())
                    .gender(user.getGender())
                    .image(user.getImage())
                    .build();
    }

    // Refresh Token으로 Access Token 재발급
    public String refreshAccessToken(Long userId, String refreshToken) {
        // Redis에서 저장된 Refresh Token 조회
        String storedRefreshToken = refreshTokenRepository.findByUserId(userId.toString())
            .orElseThrow(() -> new RuntimeException("저장된 Refresh Token이 없습니다."));
        // Refresh Token 검증
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }
        // 새로운 Access Token 발급
        return jwtTokenProvider.generateAccessToken(userId.toString());
    }

    // 로그아웃
    public void logout(Long userId) {
        // Redis에서 Refresh Token 삭제
        refreshTokenRepository.delete(userId.toString());
    }

    // 회원가입
    public boolean signup(UserSignupRequestDTO requestDTO) {
        // 이메일 또는 닉네임 중복 체크
        boolean isAlreadySignedUp = userRepository.existsByEmail(requestDTO.getEmail()) ||
                             userRepository.existsByNickname(requestDTO.getNickname());
        if (isAlreadySignedUp) { return false; }

        // 비밀번호 암호화 및 User 엔티티 변환
        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());
        User user = requestDTO.toEntity(encodedPassword);

        // 회원 정보 저장
        userRepository.save(user);
        return true;
    }

    // 이메일 중복 체크
    public boolean isEmailExists(String email) {
        return !userRepository.existsByEmail(email);
    }

    // 닉네임 중복 체크
    public boolean isNicknameExists(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

}
