package com.linkle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.linkle.domain.dto.ProfileLinkerCountDTO;
import com.linkle.domain.dto.ProfileParticipateLinkerDTO;
import com.linkle.domain.entity.Participate;

public interface ParticipateRepository extends JpaRepository<Participate, Long> {

    // 1. 링커 참여 리스트 조회 (JPQL)
    @Query("""
        SELECT l.linkerId,
               l.name,
               l.categoryId,
                       l.memo,
                       COUNT(DISTINCT c.roomId),
                       COUNT(DISTINCT p.postId),
                       l.state,
                       l.address
                FROM Participate pt
                JOIN pt.linker l
                LEFT JOIN l.posts p
                LEFT JOIN ChatRoom c ON c.linker = l
                WHERE pt.user.userId = :userId
                GROUP BY l.linkerId, l.name, l.categoryId, l.memo, l.state, l.address
        """)
    List<Object[]> findParticipationsByUserId(@Param("userId") Long userId);

    // 2. 링커 참여 통계 조회 (JPQL) — 유지
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

    @Query("""
            SELECT l.linkerId,
                   l.name,
                   l.categoryId,
                   l.memo,
                   COUNT(DISTINCT c.roomId),
                   COUNT(DISTINCT p.postId),
                   l.state,
                   l.address
            FROM Participate pt
            JOIN pt.linker l
            LEFT JOIN l.posts p
            LEFT JOIN ChatRoom c ON c.linker = l
            WHERE pt.user.userId = :userId AND p.user.userId = :userId
            GROUP BY l.categoryId
        """)
    List<Object[]> findAllWithCountsByIdsState(@Param("userId") Long userId);

    // 3. 링커 검색 (JPQL)
    @Query("""
            SELECT l.linkerId,
                   l.name,
                   l.categoryId,
                   l.memo,
                   COUNT(DISTINCT c.roomId),
                   COUNT(DISTINCT p.postId),
                   l.state,
                   l.address
            FROM Linker l
            LEFT JOIN l.posts p
            LEFT JOIN ChatRoom c ON c.linker = l
            WHERE l.name LIKE %:word%
            GROUP BY l.linkerId, l.name, l.categoryId, l.memo, l.state, l.address
        """)
    Page<Object[]> findAllWithCountsRaw(@Param("word") String word, Pageable pageable);

    // 4. 링크별 유저 참여 여부 체크 (그대로 사용)
    Optional<Participate> findByLinker_LinkerIdAndUser_UserId(Long linkerId, Long userId);

    // 5. 회원 탈퇴용 참여 삭제 (JPQL)
    @Modifying
    @Transactional
    @Query("DELETE FROM Participate p WHERE p.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // 6. 추천 링커 리스트 조회 (JPQL)
    @Query("""
            SELECT l.linkerId,
                   l.name,
                   l.categoryId,
                   l.memo,
                   COUNT(DISTINCT c.roomId),
                   COUNT(DISTINCT p.postId),
                   l.state,
                   l.address
            FROM Linker l
            LEFT JOIN l.posts p
            LEFT JOIN ChatRoom c ON c.linker = l
            WHERE l.linkerId IN :ids
            GROUP BY l.linkerId, l.name, l.categoryId, l.memo, l.state, l.address
        """)
    List<Object[]> findAllWithCountsByIds(@Param("ids") List<Long> ids);

}
