package com.hotel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.hotel.entity.Order;
import com.hotel.entity.Room;
import com.hotel.repository.OrderRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.RoomStatusService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;

@RestController
@RequestMapping("/api/user")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomStatusService roomStatusService;

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
                    logger.info("[自动取消] 订单 {} 待支付超时，已自动取消", order.getOrderNumber());
                }
            }
        }
        if (cleaned > 0) {
            logger.info("[自动取消] 本次清理了 {} 个过期待支付订单", cleaned);
        }
    }

    // 获取所有订单（支持分页）
    @GetMapping("/orders")
    public Page<Order> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable);
    }
    
    // 按用户ID分页查询订单
    @GetMapping("/orders/user/{userId}")
    public Page<Order> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByUserId(userId, pageable);
    }
    
    // 按用户ID分页查询活跃订单（仅显示活跃的订单）
    @GetMapping("/orders/user/{userId}/active")
    public Page<Order> getActiveOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findActiveOrdersByUserId(userId, pageable);
    }

    // 客户创建订单
    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) {
        logger.info("[订单创建] 收到创建订单请求，订单数据: {}", order);
        
        // 验证房间是否存在
        if (order.getRoom() == null || order.getRoom().getId() == null) {
            logger.warn("[订单创建] 请求参数错误 - 未选择房间");
            throw new RuntimeException("请选择房间");
        }
        
        Room room = roomRepository.findById(order.getRoom().getId()).orElse(null);
        if (room == null) {
            logger.warn("[订单创建] 请求参数错误 - 房间不存在，ID: {}", order.getRoom().getId());
            throw new RuntimeException("房间不存在");
        }
        
        logger.info("[订单创建] 正在验证房间，房间号: {}, 当前状态: {}", room.getRoomNumber(), room.getStatus());
        
        // 检查房间状态是否为空房、已入住或已预订（支持续订）
        String status = room.getStatus();
        if (!"空房".equals(status) && !"已入住".equals(status) && !"已预订".equals(status)) {
            logger.warn("[订单创建] 房间不可用，房间号: {}, 状态: {}", room.getRoomNumber(), room.getStatus());
            throw new RuntimeException("该房间当前不可用，请选择其他房间");
        }
        
        // 管理员已经将房间设置为可用，直接允许预订，不检查重叠订单
        // 管理员说了算！
        
        String orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);
        order.setCreateTime(new Date());
        
        // 检查是否应该自动入住
        String finalStatus;
        if (order.getCheckInTime() != null && roomStatusService.shouldAutoCheckIn(order.getCheckInTime())) {
            order.setStatus("已入住");
            finalStatus = "已入住";
        } else {
            order.setStatus("已预订");
            finalStatus = "已预订";
        }
        
        logger.info("[订单创建] 准备保存订单 - 订单号: {}, 状态: {}, 金额: {}, 房间号: {}", 
                   orderNumber, finalStatus, order.getTotalPrice(), room.getRoomNumber());
        
        Order savedOrder = orderRepository.save(order);
        roomStatusService.updateRoomStatusByOrder(savedOrder);
        
        logger.info("[订单创建] 订单创建成功！订单ID: {}, 订单号: {}, 状态: {}", 
                   savedOrder.getId(), orderNumber, finalStatus);
        
        return savedOrder;
    }

    // 客户取消订单
    @PutMapping("/orders/{id}/cancel")
    public Order cancelOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("已取消");
        Order savedOrder = orderRepository.save(order);
        
        // 更新房间状态为可用
        if (savedOrder.getRoom() != null) {
            Room room = roomRepository.findById(savedOrder.getRoom().getId()).orElse(null);
            if (room != null) {
                room.setStatus("空房");
                roomRepository.save(room);
            }
        }
        
        return savedOrder;
    }

    // 客户提前退房
    @PutMapping("/orders/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 如果状态变为已支付，检查是否应该自动入住
        if ("已支付".equals(status) && order.getCheckInTime() != null) {
            if (roomStatusService.shouldAutoCheckIn(order.getCheckInTime())) {
                order.setStatus("已入住");
            } else {
                order.setStatus("已预订");
            }
        } else {
            order.setStatus(status);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // 统一调用房间状态更新服务
        roomStatusService.updateRoomStatusByOrder(savedOrder);
        
        return savedOrder;
    }

    // 管理员更新订单状态
    @PutMapping("/admin/orders/{id}/status")
    public Order adminUpdateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        
        // 统一调用房间状态更新服务
        roomStatusService.updateRoomStatusByOrder(savedOrder);
        
        return savedOrder;
    }

    // 客户删除订单
    @DeleteMapping("/orders/{id}")
    public Map<String, Object> deleteOrder(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 只删除订单记录，不改变房间状态
            orderRepository.deleteById(id);
            result.put("success", true);
            result.put("message", "订单删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除订单失败: " + e.getMessage());
        }
        return result;
    }

    // 客户续订订单（更新退房时间）
    @PutMapping("/orders/{id}/renew")
    public Order renewOrder(@PathVariable Long id, @RequestBody Order renewData) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        
        // 更新退房时间
        if (renewData.getCheckOutTime() != null) {
            order.setCheckOutTime(renewData.getCheckOutTime());
        }
        
        // 更新价格
        if (renewData.getTotalPrice() != null) {
            order.setTotalPrice(renewData.getTotalPrice());
        }
        
        return orderRepository.save(order);
    }
    
    // 批量删除订单
    @DeleteMapping("/orders/batch")
    public Map<String, Object> batchDeleteOrders(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> orderIds = request.get("orderIds");
            if (orderIds != null && !orderIds.isEmpty()) {
                // 批量删除订单记录，不改变房间状态
                orderRepository.deleteAllById(orderIds);
                result.put("success", true);
                result.put("message", "批量删除成功");
            } else {
                result.put("success", false);
                result.put("message", "请选择要删除的订单");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量删除失败: " + e.getMessage());
        }
        return result;
    }

    // 生成订单编号
    private String generateOrderNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        int random = (int)(Math.random() * 1000);
        return "HT" + timestamp + String.format("%03d", random);
    }
}