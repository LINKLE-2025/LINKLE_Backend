// src/main/java/com/shinhan/linkle/domain/repository/FriendRepository.java
package com.linkle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.linkle.domain.entity.Friend;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 친구 요청 보내기
    // findById

    // 친구 리스트
    @Query(value = """
            SELECT IF(f.user_id1 = :userId, f.user_id2, f.user_id1) AS friend_id
            FROM FRIEND f
            WHERE (f.user_id1 = :userId OR f.user_id2 = :userId)
              AND f.state = 'ACCEPTED'
        """, nativeQuery = true)
    List<Long> findAllFriendIds(@Param("userId") Long userId);

    // 받은 요청 조회
    @Query(value = """
            SELECT f.user_id2
            FROM FRIEND f
            WHERE f.user_id1 = :userId
              AND f.state = 'REQUESTED'
        """, nativeQuery = true)
    List<Long> findReceivedFriendRequests(@Param("userId") Long userId);

    // 보낸 요청 조회
    @Query(value = """
            SELECT f.user_id1
            FROM FRIEND f
            WHERE f.user_id2 = :userId
              AND f.state = 'REQUESTED'
        """, nativeQuery = true)
    List<Long> findSentFriendRequests(@Param("userId") Long userId);

    // 요청 수락(update)
    @Query(value = """
            SELECT *
            FROM FRIEND f
            WHERE f.friend_id = :friendId
              AND f.state = 'REQUESTED'
        """, nativeQuery = true)
    Optional<Friend> findReceivedFriend(@Param("friendId") Long friendId);

    // 요청 거절(delete)
    @Query("""
        SELECT f FROM Friend f
        WHERE (f.user1.userId = :userId1 AND f.user2.userId = :userId2)
           OR (f.user1.userId = :userId2 AND f.user2.userId = :userId1)
    """)
    Optional<Friend> findFriendRelation(Long userId1, Long userId2);

    // 친구 목록 조회
    // findAllFriendIds

    // 친구 삭제
    // deleteById
}