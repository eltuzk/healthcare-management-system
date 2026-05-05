package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomCode(String roomCode);

    boolean existsByRoomCodeAndRoomIdNot(String roomCode, Long roomId);

    List<Room> findByRoomType_RoomTypeId(Long roomTypeId);

    boolean existsByRoomType_RoomTypeId(Long roomTypeId);

    @Query("""
            select r
            from Room r
            where upper(trim(r.roomCode)) = upper(trim(:roomCode))
            """)
    Optional<Room> findByRoomCodeNormalized(@Param("roomCode") String roomCode);
}
