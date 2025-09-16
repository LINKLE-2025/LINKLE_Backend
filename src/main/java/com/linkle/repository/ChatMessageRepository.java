package com.linkle.repository;

import com.linkle.domain.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Optional<ChatMessage> findTopByRoom_RoomIdOrderByMessageIdDesc(Long roomId);

    Slice<ChatMessage> findByRoom_RoomIdOrderByMessageIdDesc(Long roomId, Pageable pageable);

    Slice<ChatMessage> findByRoom_RoomIdAndMessageIdLessThanOrderByMessageIdDesc(
        Long roomId,
        Long beforeMessageId,
        Pageable pageable
    );

    Slice<ChatMessage> findByRoom_RoomIdAndMessageIdGreaterThanOrderByMessageIdAsc(
        Long roomId,
        Long afterMessageId,
        Pageable pageable
    );

    Slice<ChatMessage> findByRoom_RoomIdAndCreatedDateAfterOrderByMessageIdDesc(
        Long roomId,
        Instant joinedAt,
        Pageable pageable
    );

    Slice<ChatMessage> findByRoom_RoomIdAndCreatedDateAfterAndMessageIdLessThanOrderByMessageIdDesc(
        Long roomId,
        Instant joinedAt,
        Long beforeMessageId,
        Pageable pageable
    );

    long countByRoom_RoomId(Long roomId);

    long countByRoom_RoomIdAndMessageIdGreaterThan(Long roomId, Long lastReadMsgId);

    // 추가: 특정 유저의 메시지 작성자 null 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE chat_message SET user_id = 0 WHERE user_id = :uid", nativeQuery = true)
    int reassignMessagesToDeleted(@Param("uid") Long userId);


}
