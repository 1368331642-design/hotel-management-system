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

import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        logger.info("=== OrderStatusTask 初始化，执行首次订单状态检查 ===");
        checkAndUpdateOrderStatus();
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void checkAndUpdateOrderStatus() {
        logger.info("开始检查订单状态...");
        
        Date now = new Date();
        int autoCancelCount = 0;
        int checkInCount = 0;
        int checkOutCount = 0;
        int expireNotifyCount = 0;
        int expiredNotifyCount = 0;
        
        // 处理待支付超时自动取消（15分钟倒计时）
        List<Order> pendingOrders = orderRepository.findByStatus("待支付");
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
                    autoCancelCount++;
                }
            }
        }
        
        // 处理自动入住：入住日14:00后自动变为"已入住"
        List<Order> reservedOrders = orderRepository.findByStatusIn(Arrays.asList("已预订", "已支付"));
        for (Order order : reservedOrders) {
            if (order.getCheckInTime() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(order.getCheckInTime());
                calendar.set(Calendar.HOUR_OF_DAY, 14);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                if (now.after(calendar.getTime())) {
                    order.setStatus("已入住");
                    orderRepository.save(order);
                    Room room = order.getRoom();
                    if (room != null) {
                        room.setStatus("已入住");
                        roomRepository.save(room);
                    }
                    checkInCount++;
                }
            }
        }
        
        // 处理退房相关：到期提醒 → 已到期 → 宽限期自动退房
        // 12:00退房，11:45到期提醒，12:00到期，13:00宽限期结束自动退房
        List<Order> checkedInOrders = orderRepository.findByStatus("已入住");
        for (Order order : checkedInOrders) {
            if (order.getCheckOutTime() == null) continue;
            Date checkOutTime = order.getCheckOutTime();
            
            // 计算宽限期结束时间 = 退房日13:00
            Calendar graceEndCal = Calendar.getInstance();
            graceEndCal.setTime(checkOutTime);
            graceEndCal.set(Calendar.HOUR_OF_DAY, 13);
            graceEndCal.set(Calendar.MINUTE, 0);
            graceEndCal.set(Calendar.SECOND, 0);
            Date graceEnd = graceEndCal.getTime();
            
            // 计算到期提醒时间 = 退房日11:45
            Calendar notifyCal = Calendar.getInstance();
            notifyCal.setTime(checkOutTime);
            notifyCal.set(Calendar.HOUR_OF_DAY, 11);
            notifyCal.set(Calendar.MINUTE, 45);
            notifyCal.set(Calendar.SECOND, 0);
            Date notifyTime = notifyCal.getTime();
            
            // 计算到期时间 = 退房日12:00
            Calendar expiredCal = Calendar.getInstance();
            expiredCal.setTime(checkOutTime);
            expiredCal.set(Calendar.HOUR_OF_DAY, 12);
            expiredCal.set(Calendar.MINUTE, 0);
            expiredCal.set(Calendar.SECOND, 0);
            Date expiredTime = expiredCal.getTime();
            
            // 宽限期结束后（13:00）自动退房
            if (now.after(graceEnd)) {
                order.setStatus("自动退房");
                orderRepository.save(order);
                Room room = order.getRoom();
                if (room != null) {
                    room.setStatus("空房");
                    roomRepository.save(room);
                }
                checkOutCount++;
                logger.info("订单 {} 已自动退房（宽限期结束）", order.getOrderNumber());
            }
            // 12:00已到期但未到13:00，标记为即将到期
            else if (now.after(expiredTime)) {
                // 在12:00-13:00宽限期内，无需额外操作，前端显示宽限期UI
            }
            // 11:45-12:00 即将到期提醒
            else if (now.after(notifyTime)) {
                expireNotifyCount++;
            }
        }
        
        logger.info("订单状态检查完成 - 待支付取消:{} 入住:{} 即将到期:{} 已到期:{} 自动退房:{}", 
                   autoCancelCount, checkInCount, expireNotifyCount, expiredNotifyCount, checkOutCount);
    }
}
