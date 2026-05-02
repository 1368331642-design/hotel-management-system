package com.hotel.service;

import com.hotel.entity.Order;
import com.hotel.entity.Room;
import com.hotel.repository.OrderRepository;
import com.hotel.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

@Service
public class RoomStatusService {

    private static final Logger logger = LoggerFactory.getLogger(RoomStatusService.class);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrderRepository orderRepository;

    public void updateRoomStatusByOrder(Order order) {
        if (order.getRoom() == null) {
            return;
        }

        Room room = roomRepository.findById(order.getRoom().getId()).orElse(null);
        if (room == null) {
            return;
        }

        String roomStatus = mapOrderStatusToRoomStatus(order.getStatus());
        room.setStatus(roomStatus);
        roomRepository.save(room);
    }

    private String mapOrderStatusToRoomStatus(String orderStatus) {
        if (orderStatus == null) {
            return "空房";
        }

        switch (orderStatus) {
            case "已预订":
            case "已支付":
            case "待支付":
                return "已预订";
            case "已入住":
                return "已入住";
            case "已完成":
            case "已取消":
            case "已退房":
            case "自动退房":
                return "空房";
            default:
                return "空房";
        }
    }

    public boolean shouldAutoCheckIn(Date checkInTime) {
        if (checkInTime == null) {
            return false;
        }

        Date now = new Date();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(checkInTime);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 14);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        Date checkInTime14 = calendar.getTime();

        return now.after(checkInTime14);
    }

    public Map<String, Object> syncAllRoomStatuses() {
        Map<String, Object> result = new HashMap<>();
        int fixed = 0;
        List<String> fixedRooms = new java.util.ArrayList<>();

        List<Order> activeOrders = orderRepository.findByStatusIn(
                Arrays.asList("已预订", "已支付", "待支付", "已入住"));

        Map<Long, Order> roomOrderMap = new HashMap<>();
        for (Order order : activeOrders) {
            if (order.getRoom() == null) continue;
            Long roomId = order.getRoom().getId();
            Order existing = roomOrderMap.get(roomId);
            if (existing == null ||
                (order.getCheckInTime() != null && existing.getCheckInTime() != null &&
                 order.getCheckInTime().after(existing.getCheckInTime()))) {
                roomOrderMap.put(roomId, order);
            }
        }

        List<Room> allRooms = roomRepository.findAll();
        for (Room room : allRooms) {
            Order activeOrder = roomOrderMap.get(room.getId());

            if (activeOrder != null) {
                String expectedStatus = mapOrderStatusToRoomStatus(activeOrder.getStatus());
                if (!expectedStatus.equals(room.getStatus())) {
                    logger.info("[同步] 房间 {} 状态从 '{}' 修正为 '{}' (订单 {})",
                            room.getRoomNumber(), room.getStatus(), expectedStatus,
                            activeOrder.getStatus());
                    room.setStatus(expectedStatus);
                    roomRepository.save(room);
                    fixed++;
                    fixedRooms.add(room.getRoomNumber());
                }
            } else {
                if (!"空房".equals(room.getStatus()) && !"已完成".equals(room.getStatus())
                        && !"维护中".equals(room.getStatus())) {
                    logger.info("[同步] 房间 {} 无活跃订单，状态从 '{}' 修正为 '空房'",
                            room.getRoomNumber(), room.getStatus());
                    room.setStatus("空房");
                    roomRepository.save(room);
                    fixed++;
                    fixedRooms.add(room.getRoomNumber());
                }
            }
        }

        result.put("fixed", fixed);
        result.put("fixedRooms", fixedRooms);
        result.put("message", fixed > 0 ? "已修复 " + fixed + " 个房间状态" : "所有房间状态一致，无需修复");
        logger.info("[同步] 房间状态同步完成，修复了 {} 个房间", fixed);
        return result;
    }
}
