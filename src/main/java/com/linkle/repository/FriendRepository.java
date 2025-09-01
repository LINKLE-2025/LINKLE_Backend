// src/main/java/com/shinhan/linkle/domain/repository/FriendRepository.java
package com.linkle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkle.domain.entity.Friend;

public interface FriendRepository extends JpaRepository<Friend, Long> {

}
