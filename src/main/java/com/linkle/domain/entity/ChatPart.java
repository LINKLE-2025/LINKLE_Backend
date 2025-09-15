package com.linkle.domain.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
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
    private User user;

    // ➜ updatable=false 제거, @CreationTimestamp 제거
    @Column(name = "joined_date", nullable = false, columnDefinition = "TIMESTAMP(3)")
    private Instant joinedDate;

    @Column(name = "left_date", columnDefinition = "TIMESTAMP(3)")
    private Instant leftDate;

    @Column(name = "last_read_msg_id")
    private Long lastReadMsgId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alarm", nullable = false)
    private Alarm alarm = Alarm.ON;

    @PrePersist
    void prePersist() {
        if (joinedDate == null) joinedDate = Instant.now();
    }
}
