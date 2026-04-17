package com.hotel.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String idCard;
    
    @Column(columnDefinition = "TEXT")
    private String avatar;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;
    
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Order> orders;
}