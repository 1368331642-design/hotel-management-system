package com.hotel.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "order_table")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String orderNumber;
    private Date createTime;
    private Date checkInTime;
    private Date checkOutTime;
    private Double totalPrice;
    private String status; // 待支付、已支付、已完成、已取消
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}