// src/main/java/com/shinhan/linkle/domain/repository/FriendRepository.java
package com.linkle.repository;

import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.FriendState;import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {

}
