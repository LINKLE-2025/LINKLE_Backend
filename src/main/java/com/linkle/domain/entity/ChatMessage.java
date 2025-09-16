package com.linkle.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
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
    @ManyToOne(optional = true, fetch = FetchType.LAZY) // null/고아 허용
    @JoinColumn(
        name = "user_id",
        nullable = true,
        // 앞으로 하이버네이트가 스키마 생성할 때 FK를 만들지 않도록
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @NotFound(action = NotFoundAction.IGNORE) // DB에 유저가 없어도 예외 대신 null로 취급
    private User userId; // (요청대로 필드명 유지)

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
