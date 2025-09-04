// src/main/java/com/shinhan/linkle/domain/repository/BoardRepository.java
package com.linkle.repository;

import com.linkle.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
