package com.hotel.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/password")
public class PasswordGenController {

    @GetMapping("/generate")
    public Map<String, String> generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPassword = "123456";
        
        Map<String, String> result = new HashMap<>();
        result.put("plain", plainPassword);
        
        // 生成几个不同的加密结果（BCrypt每次结果不同，但都能验证）
        for (int i = 0; i < 3; i++) {
            result.put("encrypted_" + (i+1), encoder.encode(plainPassword));
        }
        
        return result;
    }
}
