package com.linkle.domain.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "LINKER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Linker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "linker_id")
    private Long linkerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address_name")
    private String addressName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "address_detail", nullable = true)
    private String addressDetail;

    @Column(name = "location_x", nullable = false)
    private Double locationX;

    @Column(name = "location_y", nullable = false)
    private Double locationY;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private LinkerState state;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "memo")
    private String memo;

    @OneToMany(mappedBy = "linker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participate> participants;

    @OneToMany(mappedBy = "linker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    // 엔티티 저장 직전에 기본값 설정
    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now(); // 생성 시간 자동 세팅
        }
        if (state == null) {
            state = LinkerState.ACTIVATED; // 기본 상태 설정
        }
    }
}
