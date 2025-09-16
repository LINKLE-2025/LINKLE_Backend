package com.linkle.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.linkle.domain.entity.DmPair;

public interface DmPairRepository extends JpaRepository<DmPair, Long> {
    Optional<DmPair> findBySmallerIdAndGreaterId(Long smallerId, Long greaterId);
    Optional<DmPair> findByRoom_RoomId(Long roomId);

    // 해당 유저가 포함된 모든 DM pair 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM DmPair dp WHERE dp.userAId = :uid OR dp.userBId = :uid")
    int deleteAllByUserId(@Param("uid") Long userId);
}

