package com.hotel.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "hotel_info")
public class HotelInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String address;
    private String phone;
    private String email;
    private String description;
}
