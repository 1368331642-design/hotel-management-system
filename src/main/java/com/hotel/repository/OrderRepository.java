package com.hotel.repository;

import com.hotel.entity.Order;
import com.hotel.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
    
    // 按用户ID分页查询活跃订单（不包括已取消和已完成的）
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN ('已预订', '已入住', '已支付') ORDER BY o.checkInTime ASC")
    Page<Order> findActiveOrdersByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.room = :room AND o.status IN ('已预订', '已入住') " +
           "AND NOT (o.checkOutTime <= :checkIn OR o.checkInTime >= :checkOut)")
    List<Order> findOverlappingOrders(@Param("room") Room room, 
                                        @Param("checkIn") Date checkIn, 
                                        @Param("checkOut") Date checkOut);

    @Query("SELECT o FROM Order o WHERE o.room = :room AND o.status IN ('已预订', '已入住') " +
           "AND o.checkOutTime > :now ORDER BY o.checkOutTime ASC")
    List<Order> findEarliestCheckoutOrder(@Param("room") Room room, @Param("now") Date now);
    
    // ========== 财务统计查询方法 ==========
    // 收入计算规则:
    //   - 计入收入: 已预订、已支付、已入住、已完成、自动退房 (提前退房或自动退房，金额不退，仍计入收入)
    //   - 不计入收入: 已取消 (用户取消预定，金额从总收入中剔除)
    //   - 不计入收入: 待支付 (未完成支付，不计入)
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status IN ('已预订', '已支付', '已入住', '已完成', '自动退房')")
    BigDecimal calculateTotalRevenue();
    
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status IN ('已预订', '已支付', '已入住', '已完成', '自动退房') " +
           "AND DATE(o.createTime) = CURRENT_DATE")
    BigDecimal calculateTodayRevenue();
    
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status IN ('已预订', '已支付', '已入住', '已完成', '自动退房') " +
           "AND MONTH(o.createTime) = MONTH(CURRENT_DATE) AND YEAR(o.createTime) = YEAR(CURRENT_DATE)")
    BigDecimal calculateMonthRevenue();
    
    @Query("SELECT COUNT(o) FROM Order o")
    Long countTotalOrders();
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN ('已预订', '已支付', '已入住', '已完成', '自动退房')")
    Long countPaidOrders();
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = '待支付'")
    Long countPendingOrders();
    
    @Query("SELECT o FROM Order o WHERE o.status IN ('已预订', '已支付', '已入住', '已完成', '自动退房') ORDER BY o.createTime DESC")
    List<Order> findAllPaidOrders();
    
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createTime DESC")
    Page<Order> findByStatusOrderByCreateTimeDesc(@Param("status") String status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createTime DESC")
    Page<Order> findByStatusInOrderByCreateTimeDesc(@Param("statuses") List<String> statuses, Pageable pageable);
    
    @Query("SELECT o FROM Order o ORDER BY o.createTime DESC")
    Page<Order> findAllOrderByCreateTimeDesc(Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status != '已支付' ORDER BY o.createTime DESC")
    Page<Order> findAllExceptPayedOrderByCreateTimeDesc(Pageable pageable);
}
