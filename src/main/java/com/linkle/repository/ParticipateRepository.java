// src/main/java/com/shinhan/linkle/domain/repository/ParticipateRepository.java
package com.linkle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.linkle.domain.dto.ProfileLinkerCountDTO;
import com.linkle.domain.dto.ProfileParticipateLinkerDTO;
import com.linkle.domain.dto.SearchLinkerResponseDTO;
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

    // 링커 검색
    @Query(value = """
        SELECT
            l.linker_id,
            l.name,
            l.category_id,
            l.memo,
            COUNT(DISTINCT c.room_id) AS chatRoomCount,
            COUNT(DISTINCT p.post_id) AS postCount
        FROM linker l
        LEFT JOIN post p ON p.linker_id = l.linker_id
        LEFT JOIN chatting_room c ON c.linker_id = l.linker_id
        WHERE l.name LIKE :word
        GROUP BY l.linker_id, l.name, l.category_id, l.memo
        """, nativeQuery = true)
    List<Object[]> findAllWithCountsRaw(@Param("word") String word);

    // 🔹 링크별 유저 참여 여부 체크
    Optional<Participate> findByLinker_LinkerIdAndUser_UserId(Long linkerId, Long userId);

    // 회원 탈퇴를 위한 참여 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM Participate p WHERE p.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);


}
