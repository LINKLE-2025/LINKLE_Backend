package com.linkle.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.linkle.domain.dto.UserLoginRequestDTO;
import com.linkle.domain.dto.UserSignupRequestDTO;
import com.linkle.domain.entity.User;
import com.linkle.repository.UserRepository;
import com.linkle.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인 처리 및 JWT 토큰 발급
    public String login(UserLoginRequestDTO request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        System.out.println("입력 비밀번호: " + request.getPassword());
        System.out.println("저장된 해시: " + user.getPassword());
        System.out.println("매치 결과: " + passwordEncoder.matches(request.getPassword(), user.getPassword()));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        // JWT 토큰 발급
        return jwtTokenProvider.generateToken(user.getEmail());
    }

    // 회원가입
    public boolean signup(UserSignupRequestDTO userSignupRequestDTO) {
        // 이메일 또는 닉네임 중복 체크
        boolean isAlreadySignedUp = userRepository.existsByEmail(userSignupRequestDTO.getEmail()) ||
                             userRepository.existsByNickname(userSignupRequestDTO.getNickname());
        if (isAlreadySignedUp) { return false; }

        // 비밀번호 암호화 및 User 엔티티 변환
        String encodedPassword = passwordEncoder.encode(userSignupRequestDTO.getPassword());
        User user = userSignupRequestDTO.toEntity(encodedPassword);

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
