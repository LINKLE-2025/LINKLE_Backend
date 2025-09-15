package com.linkle.repository;

import com.linkle.domain.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;   // ✅ 추가
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

    // joinedDate 이후만 가져오는 메서드 (최신부터)
    Slice<ChatMessage> findByRoom_RoomIdAndCreatedDateAfterOrderByMessageIdDesc(
        Long roomId,
        Instant joinedAt,
        Pageable pageable
    );

    // joinedDate 이후 + 커서(beforeId) 조합
    Slice<ChatMessage> findByRoom_RoomIdAndCreatedDateAfterAndMessageIdLessThanOrderByMessageIdDesc(
        Long roomId,
        Instant joinedAt,
        Long beforeMessageId,
        Pageable pageable
    );

    // 미확인 개수 계산용(커서 기반)
    long countByRoom_RoomId(Long roomId);

    long countByRoom_RoomIdAndMessageIdGreaterThan(Long roomId, Long lastReadMsgId);
}
