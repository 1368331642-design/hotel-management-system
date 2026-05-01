package com.hotel.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "service_log")
public class ServiceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String type;
    private String content;
    private String status;
    
    // 评分（1-5）
    private Integer rating;
    
    // 评价内容
    @Column(length = 500)
    private String review;
    
    // 评价图片URL列表（JSON数组字符串）
    @Column(length = 2000)
    private String images;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
}
