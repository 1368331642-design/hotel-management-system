package com.hotel.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String roomNumber;
    private String status; // 可用、已预订、已入住、维护中
    
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;
    
    @JsonIgnore
    @OneToMany(mappedBy = "room")
    private List<Order> orders;
}