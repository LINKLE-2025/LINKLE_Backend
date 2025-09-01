// src/main/java/com/shinhan/linkle/domain/repository/BoardRepository.java
package com.linkle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkle.domain.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
