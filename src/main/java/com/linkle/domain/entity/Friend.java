package com.linkle.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private FriendState state = FriendState.REQUESTED; // 기본값 ACTIVE
}
