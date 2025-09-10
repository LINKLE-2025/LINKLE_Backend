// src/main/java/com/shinhan/linkle/domain/repository/ReplyRepository.java
package com.linkle.repository;

import com.linkle.domain.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    // 회원 탈퇴를 위한 게시글 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM Reply r WHERE r.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
