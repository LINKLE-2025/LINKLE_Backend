// src/main/java/com/shinhan/linkle/domain/repository/ReplyRepository.java
package com.linkle.repository;

import com.linkle.domain.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
}
