package com.linkle.repository;

import com.linkle.domain.entity.ChatPart;
import com.linkle.domain.entity.ChatPartId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatPartRepository extends JpaRepository<ChatPart, ChatPartId> {

    Optional<ChatPart> findByRoom_RoomIdAndUser_UserId(Long roomId, Long userId);

    long countByRoom_RoomIdAndLeftDateIsNull(Long roomId);

    @EntityGraph(attributePaths = {"user"})
    List<ChatPart> findByRoom_RoomIdAndLeftDateIsNull(Long roomId);

    // 멤버 리스트용: User를 한 번에 로딩 (N+1 방지)
    @Query("""
           select cp
           from ChatPart cp
           join fetch cp.user u
           where cp.room.roomId = :roomId
             and cp.leftDate is null
           """)
    List<ChatPart> findActiveByRoomIdWithUser(@Param("roomId") Long roomId);

    // 특정 메시지를 읽은 사람 수 (읽음 마크용)
    @Query("""
           select count(cp)
           from ChatPart cp
           where cp.room.roomId = :roomId
             and cp.leftDate is null
             and cp.lastReadMsgId is not null
             and cp.lastReadMsgId >= :messageId
           """)
    long countReadersOfMessage(@Param("roomId") Long roomId,
        @Param("messageId") Long messageId);

    List<ChatPart> findByUser_UserIdAndLeftDateIsNull(Long userId);

    // 방에 남아있는 멤버들의 userId만 뽑기 (리스트 갱신 브로드캐스트용)
    @Query("""
           select u.userId
           from ChatPart cp
           join cp.user u
           where cp.room.roomId = :roomId
             and cp.leftDate is null
           """)
    List<Long> findUserIdsByRoomId(@Param("roomId") Long roomId);

    // 유저가 참가한 모든 참여행 삭제 (조인 테이블은 '0 치환'보다 삭제가 안전)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM chat_part WHERE user_id = :uid", nativeQuery = true)
    int deleteByUserId(long uid);

}
