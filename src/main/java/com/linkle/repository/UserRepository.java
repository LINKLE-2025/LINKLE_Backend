// src/main/java/com/shinhan/linkle/domain/repository/UserRepository.java
package com.linkle.repository;

import java.util.List;
import com.linkle.domain.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    // 검색에서 활용하기 위한 레포지토리
    @Query("""
            SELECT u
            FROM User u
            WHERE u.nickname LIKE %:word%
               OR u.name LIKE %:word%
        """)
    List<User> searchByNicknameOrName(@Param("word") String word);
}
