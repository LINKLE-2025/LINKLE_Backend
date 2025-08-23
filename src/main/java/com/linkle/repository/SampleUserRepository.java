package com.linkle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkle.entity.SampleUserEntity;

public interface SampleUserRepository extends JpaRepository<SampleUserEntity, Long> {
	boolean existsByEmail(String email);
}
