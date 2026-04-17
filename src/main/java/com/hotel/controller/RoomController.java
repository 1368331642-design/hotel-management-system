package com.hotel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.hotel.entity.RoomType;
import com.hotel.entity.Room;
import com.hotel.entity.Order;
import com.hotel.entity.HotelInfo;
import com.hotel.repository.RoomTypeRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.OrderRepository;
import com.hotel.repository.HotelInfoRepository;

import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@RequestMapping("/api/user")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private HotelInfoRepository hotelInfoRepository;

    // 获取所有房型
    @GetMapping("/room-types")
    public List<RoomType> getRoomTypes() {
        return roomTypeRepository.findAll();
    }

    // 根据ID获取房型
    @GetMapping("/room-types/{id}")
    public RoomType getRoomTypeById(@PathVariable Long id) {
        return roomTypeRepository.findById(id).orElse(null);
    }

    // 获取所有房间（支持分页）
    @GetMapping("/rooms")
    public Page<Room> getRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roomRepository.findAll(pageable);
    }

    // 管理员添加房型
    @PostMapping("/admin/room-types")
    public RoomType addRoomType(@RequestBody RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    // 管理员删除房型
    @DeleteMapping("/admin/room-types/{id}")
    public void deleteRoomType(@PathVariable Long id) {
        roomTypeRepository.deleteById(id);
    }

    // 管理员添加房间
    @PostMapping("/admin/rooms")
    public Room addRoom(@RequestBody Room room) {
        return roomRepository.save(room);
    }

    // 管理员更新房间状态
    @PutMapping("/admin/rooms/{id}/status")
    public Room updateRoomStatus(@PathVariable Long id, @RequestParam String status) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        room.setStatus(status);
        return roomRepository.save(room);
    }

    // 获取酒店信息
    @GetMapping("/hotel-info")
    public HotelInfo getHotelInfo() {
        List<HotelInfo> list = hotelInfoRepository.findAll();
        return list.isEmpty() ? null : list.get(0);
    }

    // 获取特定时间段可用的房间
    @GetMapping("/rooms/available")
    public Map<String, Object> getAvailableRooms(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date checkInDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date checkOutDate) {
        
        logger.info("=== 查询可用房间 ===");
        logger.info("请求入住时间: {}", sdf.format(checkInDate));
        logger.info("请求退房时间: {}", sdf.format(checkOutDate));
        
        Map<String, Object> result = new HashMap<>();
        List<Room> allRooms = roomRepository.findAll();
        List<Room> availableRooms = new ArrayList<>();
        Map<Long, Date> roomEarliestCheckout = new HashMap<>();
        
        Date now = new Date();
        
        for (Room room : allRooms) {
            if (room.getRoomType() == null) continue;
            
            logger.info("检查房间: {}", room.getRoomNumber());
            
            // 检查是否有与请求时间重叠的订单
            List<Order> overlappingOrders = orderRepository.findOverlappingOrders(room, checkInDate, checkOutDate);
            
            if (overlappingOrders.isEmpty()) {
                logger.info("  房间 {} 可用 - 无重叠订单", room.getRoomNumber());
                // 没有重叠订单，房间可用
                availableRooms.add(room);
            } else {
                logger.info("  房间 {} 不可用 - 找到 {} 个重叠订单", room.getRoomNumber(), overlappingOrders.size());
                for (Order order : overlappingOrders) {
                    logger.info("    - 订单{}: 入住={}, 退房={}, 状态={}", 
                            order.getId(), 
                            sdf.format(order.getCheckInTime()), 
                            sdf.format(order.getCheckOutTime()), 
                            order.getStatus());
                }
                // 有重叠订单，查找最早退房时间
                List<Order> earliestOrders = orderRepository.findEarliestCheckoutOrder(room, now);
                if (!earliestOrders.isEmpty()) {
                    roomEarliestCheckout.put(room.getId(), earliestOrders.get(0).getCheckOutTime());
                }
            }
        }
        
        logger.info("=== 查询完成 - 找到 {} 个可用房间 ===", availableRooms.size());
        
        result.put("availableRooms", availableRooms);
        result.put("roomEarliestCheckout", roomEarliestCheckout);
        
        return result;
    }
}
