// src/main/java/com/shinhan/linkle/domain/repository/ParticipateRepository.java
package com.linkle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.linkle.domain.dto.ProfileLinkerCountDTO;
import com.linkle.domain.dto.ProfileParticipateLinkerDTO;
import com.linkle.domain.entity.Participate;

public interface ParticipateRepository extends JpaRepository<Participate, Long> {
    // 링커 참여 리스트 조회
    @Query("""
            SELECT new com.linkle.domain.dto.ProfileParticipateLinkerDTO(
                l.linkerId, l.name, l.state, l.memo, p.participatedDate, l.categoryId
            )
            FROM Participate p
            JOIN p.linker l
            WHERE p.user.userId = :userId
        """)
    List<ProfileParticipateLinkerDTO> findParticipationsByUserId(@Param("userId") Long userId);

    // 링커 참여 통계 조회
    @Query("""
        SELECT new com.linkle.domain.dto.ProfileLinkerCountDTO(
            l.categoryId, COUNT(p)
        )
        FROM Participate p
        JOIN p.linker l
        WHERE p.user.userId = :userId
        GROUP BY l.categoryId
        ORDER BY COUNT(p) DESC
       """)
    List<ProfileLinkerCountDTO> countUserParticipationByCategory(@Param("userId") Long userId);

    // 🔹 링크별 유저 참여 여부 체크
    Optional<Participate> findByLinker_LinkerIdAndUser_UserId(Long linkerId, Long userId);

}
