package com.linkle.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.linkle.domain.entity.Participate;

public interface ParticipateByRecommendRepository extends JpaRepository<Participate, Long> {

    /**
     * 내가 가장 많이 참여한 카테고리 상위 N개
     */
    @Query("""
        SELECT l.categoryId
        FROM Participate p
        JOIN p.linker l
        WHERE p.user.userId = :userId
        GROUP BY l.categoryId
        ORDER BY COUNT(p) DESC
        """)
    List<Integer> findTopCategoriesByUser(@Param("userId") Long userId);

    /**
     * 내가 특정 날짜 이후 참여한 링커 ID들 (중복 제거)
     */
    @Query("""
        SELECT DISTINCT p.linker.linkerId
        FROM Participate p
        WHERE p.user.userId = :userId
          AND p.participatedDate >= :sinceDate
        """)
    List<Long> findRecentParticipatedLinkerIds(
        @Param("userId") Long userId,
        @Param("sinceDate") LocalDate sinceDate
    );

    /**
     * 사용자의 전체 참여 이력 (AI 추천용)
     */
    @Query("""
        SELECT p
        FROM Participate p
        JOIN FETCH p.linker
        WHERE p.user.userId = :userId
        """)
    List<Participate> findByUserId(@Param("userId") Long userId);

    /**
     * 친구들이 자주 참여한 카테고리
     */
    @Query("""
        SELECT l.categoryId
        FROM Participate p
        JOIN p.linker l
        WHERE p.user.userId IN :friendIds
        GROUP BY l.categoryId
        ORDER BY COUNT(p) DESC
        """)
    List<Integer> findTopCategoriesByFriends(@Param("friendIds") List<Long> friendIds);

    /**
     * 친구들이 자주 참여한 링커
     */
    @Query("""
        SELECT p.linker.linkerId
        FROM Participate p
        WHERE p.user.userId IN :friendIds
        GROUP BY p.linker.linkerId
        ORDER BY COUNT(p) DESC
        """)
    List<Long> findTopLinkerIdsByFriends(@Param("friendIds") List<Long> friendIds);
}
