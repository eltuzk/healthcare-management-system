package com.healthcare.backend.repository;

import com.healthcare.backend.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    boolean existsByRoomTypeName(String roomTypeName);

    boolean existsByRoomTypeNameAndRoomTypeIdNot(String roomTypeName, Long roomTypeId);
}
