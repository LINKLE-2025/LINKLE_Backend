// src/main/java/com/shinhan/linkle/domain/repository/UserRepository.java
package com.linkle.repository;

import com.linkle.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
