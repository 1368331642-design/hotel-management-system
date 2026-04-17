package com.hotel.controller;

import com.hotel.entity.Order;
import com.hotel.entity.Room;
import com.hotel.repository.OrderRepository;
import com.hotel.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping("/orders")
    public Map<String, Object> getAllOrders() {
        Map<String, Object> result = new HashMap<>();
        
        logger.info("=== 查看所有订单 ===");
        
        List<String> statuses = new ArrayList<>();
        statuses.add("已预订");
        statuses.add("已入住");
        
        List<Order> orders = orderRepository.findByStatusIn(statuses);
        
        logger.info("找到 {} 个活跃订单", orders.size());
        
        for (Order order : orders) {
            logger.info("订单ID: {}, 房间: {}, 入住: {}, 退房: {}, 状态: {}",
                    order.getId(),
                    order.getRoom() != null ? order.getRoom().getRoomNumber() : "null",
                    order.getCheckInTime() != null ? sdf.format(order.getCheckInTime()) : "null",
                    order.getCheckOutTime() != null ? sdf.format(order.getCheckOutTime()) : "null",
                    order.getStatus());
        }
        
        result.put("orders", orders);
        return result;
    }

    @GetMapping("/rooms")
    public Map<String, Object> getAllRooms() {
        Map<String, Object> result = new HashMap<>();
        
        logger.info("=== 查看所有房间 ===");
        
        List<Room> rooms = roomRepository.findAll();
        
        for (Room room : rooms) {
            logger.info("房间ID: {}, 房号: {}, 状态: {}",
                    room.getId(),
                    room.getRoomNumber(),
                    room.getStatus());
        }
        
        result.put("rooms", rooms);
        return result;
    }
}
