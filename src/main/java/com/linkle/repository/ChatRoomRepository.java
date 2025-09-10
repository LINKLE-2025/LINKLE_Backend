package com.linkle.repository;

import com.linkle.domain.entity.ChatRoom;
import com.linkle.domain.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Page<ChatRoom> findByRoomType(RoomType roomType, Pageable pageable);

    @Query("""
           select r
           from ChatRoom r
           join ChatPart p on p.room = r
           where p.user.userId = :userId
             and p.leftDate is null
           order by r.createdDate desc
           """)
    List<ChatRoom> findActiveRoomsByUserId(@Param("userId") Long userId);

    List<ChatRoom> findByLinker_LinkerIdOrderByCreatedDateDesc(Long linkerId);

    @Query("""
    select r
    from ChatRoom r
    where r.linker.linkerId = :linkerId
      and (:type is null or r.roomType = :type)
    order by r.createdDate desc
    """)
    List<ChatRoom> findRoomsByLinkerIdAndOptionalType(@Param("linkerId") Long linkerId,
        @Param("type") RoomType type);

    Page<ChatRoom> findByLinker_LinkerId(Long linkerId, Pageable pageable);
}
