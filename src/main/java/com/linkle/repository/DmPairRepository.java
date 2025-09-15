package com.linkle.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.linkle.domain.entity.DmPair;

public interface DmPairRepository extends JpaRepository<DmPair, Long> {
    Optional<DmPair> findBySmallerIdAndGreaterId(Long smallerId, Long greaterId);
    Optional<DmPair> findByRoom_RoomId(Long roomId);
}

