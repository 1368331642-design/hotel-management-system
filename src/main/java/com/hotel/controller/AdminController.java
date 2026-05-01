package com.hotel.controller;

import com.hotel.entity.HotelInfo;
import com.hotel.entity.Order;
import com.hotel.entity.Room;

import com.hotel.repository.HotelInfoRepository;
import com.hotel.repository.OrderRepository;
import com.hotel.repository.RoomRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private HotelInfoRepository hotelInfoRepository;


    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoomRepository roomRepository;

    // 清理过期的待支付订单（15分钟未支付自动取消）
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
            System.out.println("[自动取消] 管理员查询时清理了 " + cleaned + " 个过期待支付订单");
        }
    }

    @GetMapping("/hotel-info")
    public HotelInfo getHotelInfo() {
        List<HotelInfo> list = hotelInfoRepository.findAll();
        return list.isEmpty() ? null : list.get(0);
    }

    @PostMapping("/hotel-info")
    public HotelInfo saveHotelInfo(@RequestBody HotelInfo hotelInfo) {
        List<HotelInfo> list = hotelInfoRepository.findAll();
        if (!list.isEmpty()) {
            hotelInfo.setId(list.get(0).getId());
        }
        return hotelInfoRepository.save(hotelInfo);
    }





    // 获取所有订单（管理员端，支持分页，按ID降序）
    @GetMapping("/orders")
    public Page<Order> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        cleanupExpiredPendingOrders();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return orderRepository.findAll(pageable);
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        List<Order> orders = orderRepository.findAll();
        System.out.println("=== 订单统计调试信息 ===");
        System.out.println("总订单数: " + orders.size());
        int totalOrders = orders.size();
        int totalBookings = 0;
        int totalCancellations = 0;
        int totalCheckIn = 0;
        int totalCompleted = 0;
        
        for (Order order : orders) {
            System.out.println("订单ID: " + order.getId() + ", 订单号: " + order.getOrderNumber() + ", 状态: " + order.getStatus());
            if ("已预订".equals(order.getStatus())) {
                totalBookings++;
            } else if ("已取消".equals(order.getStatus())) {
                totalCancellations++;
            } else if ("已入住".equals(order.getStatus())) {
                totalCheckIn++;
            } else if ("已完成".equals(order.getStatus()) || "已退房".equals(order.getStatus()) || "自动退房".equals(order.getStatus())) {
                totalCompleted++;
            }
        }
        
        System.out.println("统计结果: 已预订=" + totalBookings + ", 已取消=" + totalCancellations + 
                          ", 已入住=" + totalCheckIn + ", 已完成=" + totalCompleted);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("totalBookings", totalBookings);
        stats.put("totalCancellations", totalCancellations);
        stats.put("totalCheckIn", totalCheckIn);
        stats.put("totalCompleted", totalCompleted);
        return stats;
    }

    @PutMapping("/orders/{id}/status")
    public Order adminUpdateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        
        Room room = order.getRoom();
        if (room != null) {
            if ("已预订".equals(status)) {
                room.setStatus("已预订");
            } else if ("已入住".equals(status)) {
                room.setStatus("已入住");
            } else if ("已完成".equals(status) || "已取消".equals(status) || "已退房".equals(status) || "自动退房".equals(status)) {
                room.setStatus("空房");
            }
            roomRepository.save(room);
        }
        
        return savedOrder;
    }

    @PostMapping("/sync-room-status")
    public Map<String, Object> syncRoomStatus() {
        List<Room> rooms = roomRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        
        int updatedCount = 0;
        
        for (Room room : rooms) {
            // 查找该房间是否有活跃订单
            String roomStatus = "空房";
            
            for (Order order : orders) {
                if (order.getRoom() != null && order.getRoom().getId().equals(room.getId())) {
                    if ("已预订".equals(order.getStatus()) || "已入住".equals(order.getStatus())) {
                        roomStatus = order.getStatus();
                        break;
                    }
                }
            }
            
            if (!roomStatus.equals(room.getStatus())) {
                room.setStatus(roomStatus);
                roomRepository.save(room);
                updatedCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRooms", rooms.size());
        result.put("updatedRooms", updatedCount);
        return result;
    }
}
