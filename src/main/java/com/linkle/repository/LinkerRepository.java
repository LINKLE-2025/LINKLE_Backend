// src/main/java/com/shinhan/linkle/domain/repository/LinkerRepository.java
package com.linkle.repository;

import java.util.List;

import com.linkle.domain.entity.Linker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;

public interface LinkerRepository extends JpaRepository<Linker, Long> {
    boolean existsByName(String name);

    // 위도(lat) = locationY, 경도(lng) = locationX
    @Query(value = """
        SELECT linker_id FROM linker
        WHERE (6371 * acos(
            cos(radians(:lat)) * cos(radians(location_y)) *
            cos(radians(location_x) - radians(:lng)) +
            sin(radians(:lat)) * sin(radians(location_y))
        )) < :radiusKm
        """, nativeQuery = true)
    List<Long> findIdsWithinRadius(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radiusKm") double radiusKm);
}
