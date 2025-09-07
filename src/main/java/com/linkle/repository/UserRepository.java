// src/main/java/com/shinhan/linkle/domain/repository/UserRepository.java
package com.linkle.repository;

import java.util.List;
import com.linkle.domain.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

}
