package com.hotel.service;

import com.hotel.entity.Order;
import com.hotel.entity.Room;
import com.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RoomStatusService {

    @Autowired
    private RoomRepository roomRepository;

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
}
