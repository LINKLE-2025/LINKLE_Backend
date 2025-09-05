// src/main/java/com/shinhan/linkle/domain/repository/PostRepository.java
package com.linkle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkle.domain.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

   List<Post> findByLinkerLinkerId(Long linkerId);

   // 프로필에서 유저 아이디를 통해 포스트 조회
   List<Post> findByUserUserIdOrderByCreatedDateDesc(Long userId);
}
