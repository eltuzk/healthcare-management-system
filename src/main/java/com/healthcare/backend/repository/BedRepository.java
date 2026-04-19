package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByRoom_RoomId(Long roomId);

    boolean existsByRoom_RoomId(Long roomId);
}