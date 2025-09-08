package com.linkle.service;

import com.linkle.domain.dto.UnreadCountResponseDTO;
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

    /** 내 전체 미확인 집계 + 방별 미확인 목록 */
    @Transactional(readOnly = true)
    public UnreadCountResponseDTO unreadSummary(Long meId) {

        // 내가 참여 중(탈퇴 X)인 모든 방의 내 ChatPart
        List<ChatPart> parts = chatPartRepository.findByUser_UserIdAndLeftDateIsNull(meId);

        Map<Long, Integer> perRoom = new LinkedHashMap<>();
        for (ChatPart p : parts) {
            Long roomId = p.getRoom().getRoomId();
            Long lastRead = p.getLastReadMsgId();

            int unread = (lastRead == null)
                ? (int) chatMessageRepository.countByRoom_RoomId(roomId)
                : (int) chatMessageRepository.countByRoom_RoomIdAndMessageIdGreaterThan(roomId, lastRead);

            perRoom.put(roomId, unread);
        }

        return UnreadCountResponseDTO.fromPerRoom(perRoom);
    }
}
