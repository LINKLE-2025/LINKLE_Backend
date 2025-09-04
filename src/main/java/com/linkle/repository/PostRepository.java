// src/main/java/com/shinhan/linkle/domain/repository/PostRepository.java
package com.linkle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkle.domain.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

   List<Post> findByLinkerLinkerId(Long linkerId);
}
