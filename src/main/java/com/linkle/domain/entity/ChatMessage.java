package com.linkle.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "CHAT_MESSAGE")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    // SYSTEM(서버) 메시지일 때는 NULL
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MessageType type; // TEXT / SYSTEM

    @Lob
    @Column(name = "text", nullable = false)
    private String text;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(3)")
    private Instant createdDate;
}
