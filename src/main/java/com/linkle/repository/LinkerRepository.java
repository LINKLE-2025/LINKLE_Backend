// src/main/java/com/shinhan/linkle/domain/repository/LinkerRepository.java
package com.linkle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkle.domain.entity.Linker;

public interface LinkerRepository extends JpaRepository<Linker, Long> {

}
