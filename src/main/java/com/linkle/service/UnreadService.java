package com.linkle.service;

import com.linkle.domain.dto.UnreadCountResponse;
import com.linkle.domain.entity.ChatPart;
import com.linkle.repository.ChatMessageRepository;
import com.linkle.repository.ChatPartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnreadService {

    private final ChatPartRepository chatPartRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 내 전체 미확인 집계 + 방별 미확인 목록
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse unreadSummary(Long meId) {
        // 내가 참여 중인 방(탈퇴 X) 멤버 레코드
        List<ChatPart> parts = chatPartRepository.findActiveByRoomIdWithUser(/* roomId */ null);
        // ↑ 위 메서드는 roomId 기준이라 바로 못 씀 → 아래와 같이 userId 기준 메서드를 추가해서 쓰는 걸 추천:
        // List<ChatPart> parts = chatPartRepository.findByUser_UserIdAndLeftDateIsNull(meId);

        // 임시 구현: userId 기준 메서드를 리포지토리에 추가했다고 가정
        // List<ChatPart> parts = chatPartRepository.findByUser_UserIdAndLeftDateIsNull(meId);

        Map<Long, Integer> perRoom = new LinkedHashMap<>();
        // 위 주석처럼 userId기준 조회 메서드를 리포에 추가했다면 for문으로 집계:
        // for (ChatPart p : parts) {
        //     Long roomId = p.getRoom().getRoomId();
        //     Long lastRead = p.getLastReadMsgId();
        //     int unread = (lastRead == null)
        //             ? (int) chatMessageRepository.countByRoom_RoomId(roomId)
        //             : (int) chatMessageRepository.countByRoom_RoomIdAndMessageIdGreaterThan(roomId, lastRead);
        //     perRoom.put(roomId, unread);
        // }

        // 데모/가이드 목적: 일단 빈 응답 반환 (리포 메서드만 추가하면 위 집계가 바로 동작)
        return UnreadCountResponse.fromPerRoom(perRoom);
    }
}
