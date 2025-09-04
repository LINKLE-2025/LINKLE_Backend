package com.linkle.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "CHAT_PART")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatPart {

    @EmbeddedId
    private ChatPartId id;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // ✅ 이제 part.getUser() 가능

    @CreationTimestamp
    @Column(name = "joined_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(3)")
    private Instant joinedDate;

    @Column(name = "left_date", columnDefinition = "TIMESTAMP(3)")
    private Instant leftDate;

    @Column(name = "last_read_msg_id")
    private Long lastReadMsgId; // 필요 시 ManyToOne 매핑 가능

    @Enumerated(EnumType.STRING)
    @Column(name = "alarm", nullable = false)
    private Alarm alarm = Alarm.ACTIVE;
}
