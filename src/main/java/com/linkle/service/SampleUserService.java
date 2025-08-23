package com.linkle.sample;

import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public UserResponseDTO register(UserRequestDTO dto) {
		UserEntity user = UserEntity.builder()
			.username(dto.getUsername())
			.email(dto.getEmail())
			.password(dto.getPassword()) // 비밀번호 암호화 생략
			.build();

		UserEntity saved = userRepository.save(user);

		return UserResponseDTO.builder()
			.id(saved.getId())
			.username(saved.getUsername())
			.email(saved.getEmail())
			.build();
	}
}
