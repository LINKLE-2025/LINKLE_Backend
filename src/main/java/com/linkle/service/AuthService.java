package com.linkle.service;

import org.springframework.stereotype.Service;

import com.linkle.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    // 로그인
    public String login(String email, String password) {
        return "login";
    }

    // 회원가입
    public String signup(String email, String password, String nickname) {
        return "signup";
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
