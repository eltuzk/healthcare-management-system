package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomCode(String roomCode);

    boolean existsByRoomCodeAndRoomIdNot(String roomCode, Long roomId);

    List<Room> findByRoomType_RoomTypeId(Long roomTypeId);

    boolean existsByRoomType_RoomTypeId(Long roomTypeId);
}