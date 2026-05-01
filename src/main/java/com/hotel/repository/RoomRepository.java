package com.hotel.repository;

import com.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomNumber(String roomNumber);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = '已预订'")
    Long countReservedRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = '已入住'")
    Long countCheckedInRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = '空房'")
    Long countAvailableRooms();
}