package com.hotel.task;

import com.hotel.entity.Order;
import com.hotel.entity.Room;
import com.hotel.repository.OrderRepository;
import com.hotel.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.Arrays;

@Component
public class OrderStatusTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusTask.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void checkAndUpdateOrderStatus() {
        logger.info("开始检查订单状态...");
        
        Date now = new Date();
        
        // 处理自动入住
        List<Order> reservedOrders = orderRepository.findByStatusIn(Arrays.asList("已预订", "已支付"));
        int checkInCount = 0;
        for (Order order : reservedOrders) {
            if (order.getCheckInTime() != null) {
                // 创建一个新的日期对象，设置为入住日期的12:00:00
                Date checkInDate = order.getCheckInTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(checkInDate);
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date checkInNoon = calendar.getTime();
                
                if (now.after(checkInNoon)) {
                    order.setStatus("已入住");
                    orderRepository.save(order);
                    
                    Room room = order.getRoom();
                    if (room != null) {
                        room.setStatus("已入住");
                        roomRepository.save(room);
                        logger.info("房间 {} 状态已更新为已入住", room.getRoomNumber());
                    }
                    
                    checkInCount++;
                    logger.info("订单 {} 状态已更新为已入住", order.getOrderNumber());
                }
            }
        }
        
        // 处理自动退房
        List<Order> checkedInOrders = orderRepository.findByStatus("已入住");
        int checkOutCount = 0;
        for (Order order : checkedInOrders) {
            if (order.getCheckOutTime() != null && now.after(order.getCheckOutTime())) {
                order.setStatus("已退房");
                orderRepository.save(order);
                
                Room room = order.getRoom();
                if (room != null) {
                    room.setStatus("空房");
                    roomRepository.save(room);
                    logger.info("房间 {} 状态已更新为空房", room.getRoomNumber());
                }
                
                checkOutCount++;
                logger.info("订单 {} 状态已更新为已退房", order.getOrderNumber());
            }
        }
        
        logger.info("订单状态检查完成，共更新 {} 个入住订单，{} 个退房订单", checkInCount, checkOutCount);
    }
}
