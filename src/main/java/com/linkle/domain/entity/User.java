package com.linkle.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"nickname"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "age")
    private Integer age;

    @Column(nullable = false)
    private String gender;

    @Column
    private String image;

    @Column
    private String background;

    @Column
    private String memo;

    @Column(name = "bank_id")
    private Integer bankId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "balance")
    private Long balance;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<AccountHistory> accountHistories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDate.now();
        if (this.balance == null) {  // balance가 null이면 0으로 설정
            this.balance = 0L;
        }
    }
}
