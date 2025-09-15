package com.linkle.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "dm_pair",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_dm_normalized_pair",
            columnNames = {"smaller_id", "greater_id"}
        )
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DmPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dm_pair_id")
    private Long dmPairId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @Column(name = "user_a_id", nullable = false)
    private Long userAId;

    @Column(name = "user_b_id", nullable = false)
    private Long userBId;

    // 엔티티에서 직접 세팅 (DB 생성 컬럼 아님)
    @Column(name = "smaller_id", nullable = false)
    private Long smallerId;

    @Column(name = "greater_id", nullable = false)
    private Long greaterId;

    /** userAId/userBId가 바뀌거나 새로 저장될 때 항상 정규화 */
    @PrePersist
    @PreUpdate
    private void normalizePair() {
        if (userAId == null || userBId == null) {
            throw new IllegalStateException("userAId and userBId must not be null");
        }
        if (userAId.equals(userBId)) {
            throw new IllegalStateException("Cannot create DM with the same user");
        }
        long a = Math.min(userAId, userBId);
        long b = Math.max(userAId, userBId);
        this.smallerId = a;
        this.greaterId = b;
    }
}
