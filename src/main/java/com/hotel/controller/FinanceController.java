package com.hotel.controller;

import com.hotel.entity.Order;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.repository.OrderRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin/finance")
public class FinanceController {

    private static final Logger logger = LoggerFactory.getLogger(FinanceController.class);

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    @Autowired
    private RoomRepository roomRepository;

    // 清理过期的待支付订单
    private void cleanupExpiredPendingOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus("待支付");
        Date now = new Date();
        int cleaned = 0;
        for (Order order : pendingOrders) {
            if (order.getCreateTime() != null) {
                Calendar deadline = Calendar.getInstance();
                deadline.setTime(order.getCreateTime());
                deadline.add(Calendar.MINUTE, 15);
                if (now.after(deadline.getTime())) {
                    order.setStatus("已取消");
                    orderRepository.save(order);
                    Room room = order.getRoom();
                    if (room != null) {
                        room.setStatus("空房");
                        roomRepository.save(room);
                    }
                    cleaned++;
                }
            }
        }
        if (cleaned > 0) {
            logger.info("[财务统计-自动取消] 清理了 {} 个过期待支付订单", cleaned);
        }
    }

    // 1. 财务摘要接口
    @GetMapping("/summary")
    public Map<String, Object> getFinanceSummary() {
        logger.info("[财务统计] 收到获取财务摘要请求");
        Map<String, Object> summary = new HashMap<>();
        
        // 先清理过期的待支付订单，确保数据准确性
        cleanupExpiredPendingOrders();
        
        // 总收入: 统计所有非取消订单(已预订/已支付/已入住/已完成)的金额
        // 提前退房(status=已完成) → 金额仍计入；取消预定(status=已取消) → 金额剔除
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        summary.put("totalRevenue", totalRevenue);
        
        // 今日收入
        BigDecimal todayRevenue = orderRepository.calculateTodayRevenue();
        summary.put("todayRevenue", todayRevenue);
        
        // 本月收入
        BigDecimal monthRevenue = orderRepository.calculateMonthRevenue();
        summary.put("monthRevenue", monthRevenue);
        
        // 总订单数
        Long totalOrders = orderRepository.countTotalOrders();
        summary.put("totalOrders", totalOrders);
        
        // 已支付订单数（计入收入的订单数）
        Long paidOrders = orderRepository.countPaidOrders();
        summary.put("paidOrders", paidOrders);
        
        // 待支付订单数 - 查询 status = '待支付' 的订单
        Long pendingOrders = orderRepository.countPendingOrders();
        summary.put("pendingOrders", pendingOrders);
        logger.info("[财务统计] 待支付订单查询结果: {}", pendingOrders);
        
        // 已取消订单数
        Long cancelledOrders = orderRepository.countCancelledOrders();
        summary.put("cancelledOrders", cancelledOrders);
        
        // 已预定房间数
        Long reservedRooms = roomRepository.countReservedRooms();
        summary.put("reservedRooms", reservedRooms);
        
        // 已入住房间数
        Long checkedInRooms = roomRepository.countCheckedInRooms();
        summary.put("checkedInRooms", checkedInRooms);
        
        // 空房数
        Long availableRooms = roomRepository.countAvailableRooms();
        summary.put("availableRooms", availableRooms);
        
        // 平均客单价
        BigDecimal avgOrderPrice = BigDecimal.ZERO;
        if (paidOrders > 0) {
            avgOrderPrice = totalRevenue.divide(new BigDecimal(paidOrders), 2, RoundingMode.HALF_UP);
        }
        summary.put("avgOrderPrice", avgOrderPrice);
        
        logger.info("[财务统计] 财务摘要返回 - 总收入: {}, 今日收入: {}, 本月收入: {}, 已支付: {}, 待支付: {}", 
                  totalRevenue, todayRevenue, monthRevenue, paidOrders, pendingOrders);
        
        return summary;
    }

    // 2. 月度收入趋势
    @GetMapping("/monthly")
    public List<Map<String, Object>> getMonthlyRevenue(@RequestParam(defaultValue = "6") int months) {
        logger.info("[财务统计] 收到获取月度收入请求，参数: months={}", months);
        List<Map<String, Object>> result = new ArrayList<>();
        List<Order> allPaidOrders = orderRepository.findAllPaidOrders();
        
        // 按月份分组统计
        Map<String, BigDecimal> monthlyStats = new LinkedHashMap<>();
        
        // 生成最近N个月的时间范围
        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthKey = monthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthlyStats.put(monthKey, BigDecimal.ZERO);
        }
        
        // 统计收入
        for (Order order : allPaidOrders) {
            if (order.getCreateTime() == null) continue;
            
            LocalDate orderDate = new java.sql.Date(order.getCreateTime().getTime()).toLocalDate();
            String monthKey = orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            if (monthlyStats.containsKey(monthKey) && order.getTotalPrice() != null) {
                monthlyStats.put(monthKey, monthlyStats.get(monthKey).add(order.getTotalPrice()));
            }
        }
        
        // 转换为返回格式
        for (Map.Entry<String, BigDecimal> entry : monthlyStats.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", entry.getKey());
            item.put("revenue", entry.getValue());
            result.add(item);
        }
        
        logger.info("[财务统计] 月度收入统计返回，共{}条数据", result.size());
        return result;
    }

    // 3. 每日收入明细（用于图表）
    @GetMapping("/daily")
    public List<Map<String, Object>> getDailyRevenue(@RequestParam(defaultValue = "30") int days) {
        logger.info("[财务统计] 收到获取每日收入请求，参数: days={}", days);
        List<Map<String, Object>> result = new ArrayList<>();
        List<Order> allPaidOrders = orderRepository.findAllPaidOrders();
        
        // 按日期分组统计
        Map<String, BigDecimal> dailyStats = new LinkedHashMap<>();
        
        // 生成最近N天的时间范围
        LocalDate now = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate dayDate = now.minusDays(i);
            String dayKey = dayDate.format(DateTimeFormatter.ofPattern("MM/dd"));
            dailyStats.put(dayKey, BigDecimal.ZERO);
        }
        
        // 统计收入
        for (Order order : allPaidOrders) {
            if (order.getCreateTime() == null) continue;
            
            LocalDate orderDate = new java.sql.Date(order.getCreateTime().getTime()).toLocalDate();
            String dayKey = orderDate.format(DateTimeFormatter.ofPattern("MM/dd"));
            
            if (dailyStats.containsKey(dayKey) && order.getTotalPrice() != null) {
                dailyStats.put(dayKey, dailyStats.get(dayKey).add(order.getTotalPrice()));
            }
        }
        
        // 转换为返回格式
        for (Map.Entry<String, BigDecimal> entry : dailyStats.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("revenue", entry.getValue());
            result.add(item);
        }
        
        logger.info("[财务统计] 每日收入统计返回，共{}条数据", result.size());
        return result;
    }

    // 4. 各房型收入占比
    @GetMapping("/room-type-stats")
    public List<Map<String, Object>> getRoomTypeStats() {
        logger.info("[财务统计] 收到获取房型收入统计请求");
        List<Map<String, Object>> result = new ArrayList<>();
        List<Order> allPaidOrders = orderRepository.findAllPaidOrders();
        
        // 按房型分组统计
        Map<Long, Map<String, Object>> roomTypeStats = new HashMap<>();
        
        // 初始化所有房型
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        for (RoomType roomType : roomTypes) {
            Map<String, Object> roomTypeData = new HashMap<>();
            roomTypeData.put("roomTypeId", roomType.getId());
            roomTypeData.put("roomTypeName", roomType.getName());
            roomTypeData.put("revenue", BigDecimal.ZERO);
            roomTypeStats.put(roomType.getId(), roomTypeData);
        }
        
        // 统计各房型收入
        for (Order order : allPaidOrders) {
            if (order.getRoom() == null || order.getRoom().getRoomType() == null) continue;
            
            Long roomTypeId = order.getRoom().getRoomType().getId();
            if (roomTypeStats.containsKey(roomTypeId) && order.getTotalPrice() != null) {
                Map<String, Object> roomTypeData = roomTypeStats.get(roomTypeId);
                BigDecimal currentRevenue = (BigDecimal) roomTypeData.get("revenue");
                roomTypeData.put("revenue", currentRevenue.add(order.getTotalPrice()));
            }
        }
        
        // 转换为返回格式
        for (Map<String, Object> roomTypeData : roomTypeStats.values()) {
            result.add(roomTypeData);
        }
        
        // 按收入降序排序
        result.sort((a, b) -> {
            BigDecimal revenueA = (BigDecimal) a.get("revenue");
            BigDecimal revenueB = (BigDecimal) b.get("revenue");
            return revenueB.compareTo(revenueA);
        });
        
        logger.info("[财务统计] 房型收入统计返回，共{}条数据", result.size());
        return result;
    }

    // 5. 收入流水列表
    @GetMapping("/orders")
    public Page<Order> getFinanceOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        logger.info("[财务统计] 收到获取订单列表请求 - page: {}, size: {}, status: {}", 
                   page, size, status);
        
        cleanupExpiredPendingOrders();
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Order> result;
        if (status != null && !status.isEmpty()) {
            if ("已完成".equals(status)) {
                List<String> completedStatuses = java.util.Arrays.asList("已完成", "自动退房");
                result = orderRepository.findByStatusInOrderByCreateTimeDesc(completedStatuses, pageable);
            } else {
                result = orderRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
            }
        } else {
            result = orderRepository.findAllExceptPayedOrderByCreateTimeDesc(pageable);
        }
        
        logger.info("[财务统计] 订单列表返回 - 共{}条，当前页{}条", 
                   result.getTotalElements(), result.getContent().size());
        return result;
    }
}
