// src/main/java/com/shinhan/linkle/domain/repository/ParticipateRepository.java
package com.linkle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkle.domain.entity.Participate;

public interface ParticipateRepository extends JpaRepository<Participate, Long> {

}
