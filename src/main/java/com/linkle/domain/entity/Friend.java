package com.linkle.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FRIEND", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id1", "user_id2"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long friendId;

    @ManyToOne
    @JoinColumn(name = "user_id1", nullable = false)
    private User user1; // 요청 받은 사용자

    @ManyToOne
    @JoinColumn(name = "user_id2", nullable = false)
    private User user2; // 요청 보낸 사용자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendState state = FriendState.ACTIVE; // 기본값 ACTIVE
}
