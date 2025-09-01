// src/main/java/com/shinhan/linkle/domain/repository/UserRepository.java
package com.linkle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkle.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
