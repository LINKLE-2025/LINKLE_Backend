package com.linkle.sample;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
	boolean existsByEmail(String email);
}
