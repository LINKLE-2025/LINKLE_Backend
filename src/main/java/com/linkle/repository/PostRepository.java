// src/main/java/com/shinhan/linkle/domain/repository/PostRepository.java
package com.linkle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.linkle.domain.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

   List<Post> findByLinkerLinkerId(Long linkerId);

   // 프로필에서 유저 아이디를 통해 포스트 조회
   List<Post> findByUserUserIdOrderByCreatedDateDesc(Long userId);

   // 회원 탈퇴를 위한 작업
   @Modifying
   @Transactional
   @Query("DELETE FROM Post p WHERE p.user.userId = :userId")
   void deleteByUserId(@Param("userId") Long userId);
}
