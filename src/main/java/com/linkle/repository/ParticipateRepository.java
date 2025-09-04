// src/main/java/com/shinhan/linkle/domain/repository/ParticipateRepository.java
package com.linkle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.linkle.domain.dto.UserLinkerCountDTO;
import com.linkle.domain.dto.UserParticipateLinkerDTO;
import com.linkle.domain.entity.Participate;

public interface ParticipateRepository extends JpaRepository<Participate, Long> {
    // 링커 참여 리스트 조회
    @Query("""
            SELECT new com.linkle.domain.dto.UserParticipateLinkerDTO(
                l.linkerId, l.name, l.state, l.memo, p.participatedDate
            )
            FROM Participate p
            JOIN p.linker l
            WHERE p.user.userId = :userId
        """)
    List<UserParticipateLinkerDTO> findParticipationsByUserId(@Param("userId") Long userId);

    // 링커 참여 통계 조회
    @Query("""
        SELECT new com.linkle.domain.dto.UserLinkerCountDTO(
            l.categoryId, COUNT(p)
        )
        FROM Participate p
        JOIN p.linker l
        WHERE p.user.userId = :userId
        GROUP BY l.categoryId
        ORDER BY COUNT(p) DESC
       """)
    List<UserLinkerCountDTO> countUserParticipationByCategory(@Param("userId") Long userId);

}
