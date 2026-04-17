package com.hotel.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPassword = "123456";
        String encryptedPassword = encoder.encode(plainPassword);
        
        System.out.println("Plain password: " + plainPassword);
        System.out.println("BCrypt encrypted: " + encryptedPassword);
        
        // 验证
        boolean matches = encoder.matches(plainPassword, encryptedPassword);
        System.out.println("Verification: " + matches);
    }
}
