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

    @Query("""
           select cp
           from ChatPart cp
           join fetch cp.user u
           where cp.room.roomId = :roomId
             and cp.leftDate is null
           """)
    List<ChatPart> findActiveByRoomIdWithUser(@Param("roomId") Long roomId);

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

    @Query("""
           select u.userId
           from ChatPart cp
           join cp.user u
           where cp.room.roomId = :roomId
             and cp.leftDate is null
           """)
    List<Long> findUserIdsByRoomId(@Param("roomId") Long roomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM chat_part WHERE user_id = :uid", nativeQuery = true)
    int deleteByUserId(long uid);

    // (선택) 별칭
    default Optional<ChatPart> findByRoomIdAndUserId(Long roomId, Long userId) {
        return findByRoom_RoomIdAndUser_UserId(roomId, userId);
    }

    @Query("""
       select cp
       from ChatPart cp
       join fetch cp.user u
       where cp.room.roomId = :roomId
       """)
    List<ChatPart> findAllByRoomIdWithUser(@Param("roomId") Long roomId);


}
