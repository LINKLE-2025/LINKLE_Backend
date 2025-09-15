package com.linkle.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType; // DM / LIGHT / CLASS

    @Column(name = "room_name", length = 100)
    private String roomName;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String memo;

    @Column(name = "theme_color")
    private String themeColor;

    @Column(name = "owner_id")
    private Long ownerId; // 그룹 방 소유자 (DM이면 NULL)

    @Column(name = "entry_fee")
    private Integer entryFee;

    @Column(name = "start_date", columnDefinition = "TIMESTAMP(3)")
    private Instant startDate;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(3)")
    private Instant createdDate;

    @ManyToOne
    @JoinColumn(name = "linker_id")
    private Linker linker;
}
