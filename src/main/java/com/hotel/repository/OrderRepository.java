package com.hotel.repository;

import com.hotel.entity.Order;
import com.hotel.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(String status);
    List<Order> findByStatusIn(List<String> statuses);
    
    // 按用户ID分页查询订单
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    // 按用户ID查询所有订单
    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.room = :room AND o.status IN ('已预订', '已入住') " +
           "AND NOT (o.checkOutTime <= :checkIn OR o.checkInTime >= :checkOut)")
    List<Order> findOverlappingOrders(@Param("room") Room room, 
                                        @Param("checkIn") Date checkIn, 
                                        @Param("checkOut") Date checkOut);

    @Query("SELECT o FROM Order o WHERE o.room = :room AND o.status IN ('已预订', '已入住') " +
           "AND o.checkOutTime > :now ORDER BY o.checkOutTime ASC")
    List<Order> findEarliestCheckoutOrder(@Param("room") Room room, @Param("now") Date now);
}
