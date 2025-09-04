// src/main/java/com/shinhan/linkle/domain/repository/LinkerRepository.java
package com.linkle.repository;

import com.linkle.domain.entity.Linker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkerRepository extends JpaRepository<Linker, Long> {

}
