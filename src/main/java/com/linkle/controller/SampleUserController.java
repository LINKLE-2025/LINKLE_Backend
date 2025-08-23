package com.linkle.sample;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<UserResponseDTO> register(@RequestBody UserRequestDTO dto) {
		UserResponseDTO result = userService.register(dto);
		return ResponseEntity.ok(result);
	}
}
