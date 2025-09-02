// src/main/java/com/shinhan/linkle/domain/repository/PostRepository.java
package com.linkle.repository;

import com.linkle.domain.entity.Post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
