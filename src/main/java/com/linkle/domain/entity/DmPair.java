package com.linkle.domain.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(
    name = "DM_PAIR",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_dm_normalized_pair", columnNames = {"smaller_id", "greater_id"})
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DmPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dm_pair_id")
    private Long dmPairId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @Column(name = "user_a_id", nullable = false)
    private Long userAId;

    @Column(name = "user_b_id", nullable = false)
    private Long userBId;

    // 생성 칼럼(가상/퍼시스턴트) — DB가 채우므로 엔티티에서는 읽기 전용
    @Column(name = "smaller_id", insertable = false, updatable = false)
    private Long smallerId;

    @Column(name = "greater_id", insertable = false, updatable = false)
    private Long greaterId;
}
