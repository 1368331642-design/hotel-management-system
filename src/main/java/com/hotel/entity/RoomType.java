package com.hotel.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "room_type")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private Double price;
    private Integer capacity;
    private Integer minCapacity;
    private Integer maxCapacity;
    private String facilities;
    @Column(columnDefinition = "TEXT")
    private String image;
    
    @JsonIgnore
    @OneToMany(mappedBy = "roomType")
    private List<Room> rooms;
}