package com.hotel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.entity.User;
import com.hotel.repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            List<User> users = userRepository.findByUsername(username);
            User user = (users != null && !users.isEmpty()) ? users.get(0) : null;

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("user", user);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "登录失败: " + e.getMessage());
            return result;
        }
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        // 暂时不加密 - 先让登录能工作
        userRepository.save(user);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("message", "注册成功");
        return result;
    }
}