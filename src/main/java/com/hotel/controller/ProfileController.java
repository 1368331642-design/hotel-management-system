package com.hotel.controller;

import com.hotel.entity.User;
import com.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateProfile(@PathVariable Long id, @RequestBody Map<String, Object> profileData) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            User user = userOpt.get();

            if (profileData.containsKey("name")) {
                user.setName((String) profileData.get("name"));
            }
            if (profileData.containsKey("phone")) {
                user.setPhone((String) profileData.get("phone"));
            }
            if (profileData.containsKey("email")) {
                user.setEmail((String) profileData.get("email"));
            }
            if (profileData.containsKey("idCard")) {
                user.setIdCard((String) profileData.get("idCard"));
            }
            if (profileData.containsKey("avatar")) {
                user.setAvatar((String) profileData.get("avatar"));
            }

            User savedUser = userRepository.save(user);

            result.put("success", true);
            result.put("message", "个人信息更新成功");
            result.put("user", savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新个人信息失败: " + e.getMessage());
        }

        return result;
    }
}
