package com.linkle.domain.dto;

import com.linkle.domain.entity.ChatMessage;
import com.linkle.domain.entity.ChatRoom;
import com.linkle.domain.entity.RoomType;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponseDTO {

    // ===== 공통 필드 =====
    private Long roomId;
    private RoomType roomType;
    private String roomName;
    private Long linkerId;
    private Boolean isMember;

    // ===== 그룹/클래스 전용 =====
    private String description;
    private String memo;
    private String themeColor;
    private Integer entryFee;
    private Instant startDate;
    private Long ownerId;
    private Instant createdDate;

    // ===== 부가 정보 =====
    private Integer memberCount;
    private Integer unreadCount;
    private String lastMessagePreview;
    private Instant lastMessageDate;

    // ===== DM 전용 =====
    private Long dmPartnerId;
    private String dmPartnerName;
    private String dmPartnerProfileImageUrl;

    // ===== 기본 변환 =====
    public static RoomResponseDTO fromEntity(ChatRoom r) {
        if (r == null) return null;

        RoomResponseDTO.RoomResponseDTOBuilder builder = RoomResponseDTO.builder()
            .roomId(r.getRoomId())
            .roomType(r.getRoomType())
            .roomName(r.getRoomName());

        if (r.getRoomType() != RoomType.DM) {
            builder.description(r.getDescription())
                .memo(r.getMemo())
                .themeColor(r.getThemeColor())
                .entryFee(r.getEntryFee())
                .startDate(r.getStartDate())
                .ownerId(r.getOwnerId())
                .createdDate(r.getCreatedDate());

            if (r.getLinker() != null) {
                builder.linkerId(r.getLinker().getLinkerId());
            }

        }

        return builder.build();
    }

    // ===== 공용 메타데이터 추가 =====
    private static RoomResponseDTO withMeta(RoomResponseDTO base,
        ChatMessage lastMessage,
        Integer unreadCount,
        Integer memberCount) {
        if (base == null) return null;

        if (unreadCount != null) base.setUnreadCount(unreadCount);
        if (memberCount != null) base.setMemberCount(memberCount);

        if (lastMessage != null) {
            base.setLastMessagePreview(lastMessage.getText());      // ChatMessage.getText()
            base.setLastMessageDate(lastMessage.getCreatedDate());  // *Date 네이밍
        }
        return base;
    }

    // ===== 일반 방 변환 =====
    public static RoomResponseDTO fromEntity(ChatRoom r,
        ChatMessage lastMessage,
        Integer unreadCount,
        Integer memberCount) {
        return withMeta(fromEntity(r), lastMessage, unreadCount, memberCount);
    }

    // ===== DM 전용 변환 =====
    public static RoomResponseDTO fromDm(ChatRoom r,
        Long partnerId,
        String partnerName,
        String partnerProfileImageUrl,
        ChatMessage lastMessage,
        Integer unreadCount) {
        RoomResponseDTO base = fromEntity(r);
        if (base == null) return null;

        // DM 전용 필드 세팅
        base.setDmPartnerId(partnerId);
        base.setDmPartnerName(partnerName);
        base.setDmPartnerProfileImageUrl(partnerProfileImageUrl);

        // DM은 멤버 수 2 고정
        base.setMemberCount(2);

        return withMeta(base, lastMessage, unreadCount, 2);
    }

    // ===== 리스트 변환 =====
    public static List<RoomResponseDTO> fromEntityList(List<ChatRoom> rooms) {
        if (rooms == null) return null;
        return rooms.stream().map(RoomResponseDTO::fromEntity).collect(Collectors.toList());
    }
}
