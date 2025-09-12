package com.linkle.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "linker_id"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Participate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "linker_id", nullable = false)
    private Linker linker;

    @Column(name = "participated_date", nullable = false)
    private LocalDate participatedDate;
}
