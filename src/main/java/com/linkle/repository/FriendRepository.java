// src/main/java/com/shinhan/linkle/domain/repository/FriendRepository.java
package com.linkle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.linkle.domain.dto.FriendResponseDTO;
import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.User;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 친구 요청 보내기
    // findById

    // 친구 리스트 (userId만 반환 → 그대로 둠)
    @Query(value = """
            SELECT IF(f.user_id1 = :userId, f.user_id2, f.user_id1) AS friend_id
            FROM FRIEND f
            WHERE (f.user_id1 = :userId OR f.user_id2 = :userId)
              AND f.state = 'ACCEPTED'
        """, nativeQuery = true)
    List<Long> findAllFriendIds(@Param("userId") Long userId);

    // 받은 요청 조회 (내가 user2일 때)
    @Query(value = """
            SELECT *
            FROM FRIEND f
            WHERE f.user_id2 = :userId
              AND f.state = 'REQUESTED'
        """, nativeQuery = true)
    List<Friend> findReceivedFriendRequests(@Param("userId") Long userId);

    // 보낸 요청 조회 (내가 user1일 때)
    @Query(value = """
        SELECT *
        FROM FRIEND f
        WHERE f.user_id1 = :userId
          AND f.state = 'REQUESTED'
    """, nativeQuery = true)
    List<Friend> findSentFriendRequests(@Param("userId") Long userId);

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
    @Query("""
        SELECT f FROM Friend f
        WHERE (f.user1.userId = :userId OR f.user2.userId = :userId)
            AND f.state = 'ACCEPTED'
    """)
    List<Friend> findAcceptedFriends(@Param("userId") Long userId);


    @Query("""
        SELECT f FROM Friend f
        WHERE (f.user1.userId = :userId OR f.user2.userId = :userId)
    """)
    List<Friend> findAllFriends(@Param("userId") Long userId);


    // 친구 삭제
    // deleteById

    // 친구 몇명인지 조회

    // 친구 조회
    @Query("SELECT f FROM Friend f " +
        "WHERE (f.user1.userId = :userId1 AND f.user2.userId = :userId2) " +
        "   OR (f.user1.userId = :userId2 AND f.user2.userId = :userId1)")
    Friend findByUserPair(@Param("userId1") Long userId1, @Param("userId2") Long userId2);


    // 검색에서 활용하기 위한 레포지토리
    // 친구 검색 기능으로 FriendRepository로 이동
    @Query("""
            SELECT u
            FROM User u
            WHERE u.nickname LIKE %:word%
               OR u.name LIKE %:word%
        """)
    List<User> searchByNicknameOrName(@Param("word") String word);
}