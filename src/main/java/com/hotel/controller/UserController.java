package com.hotel.controller;

import com.hotel.entity.Role;
import com.hotel.entity.User;
import com.hotel.repository.RoleRepository;
import com.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 获取所有用户（支持分页）
    @GetMapping
    public Page<User> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return userRepository.findAll(pageable);
    }

    // 创建用户
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createUser(@RequestBody Map<String, Object> userData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String username = (String) userData.get("username");
            String password = (String) userData.get("password");
            String name = (String) userData.get("name");
            String phone = (String) userData.get("phone");
            String email = (String) userData.get("email");
            String idCard = (String) userData.get("idCard");
            String roleName = (String) userData.get("role");

            // 检查是否尝试创建管理员角色
            if ("ADMIN".equals(roleName)) {
                result.put("success", false);
                result.put("message", "不能创建管理员账户");
                return result;
            }

            // 检查用户名是否已存在
            List<User> existingUsers = userRepository.findByUsername(username);
            if (existingUsers != null && !existingUsers.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }

            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password)); // 使用BCrypt加密密码
            user.setName(name);
            user.setPhone(phone);
            user.setEmail(email);
            user.setIdCard(idCard);
            
            // 初始化角色列表
            user.setRoles(new ArrayList<>());

            // 保存用户 - JPA会自动回填ID
            User savedUser = userRepository.save(user);
            
            // 立即刷新确保ID已回填
            userRepository.flush();

            // 分配角色
            if (roleName != null) {
                Optional<Role> roleOpt = roleRepository.findFirstByName(roleName);
                if (roleOpt.isPresent()) {
                    Role role = roleOpt.get();
                    savedUser.getRoles().add(role);
                    // 再次保存用户以关联角色
                    userRepository.save(savedUser);
                    userRepository.flush();
                }
            }

            result.put("success", true);
            result.put("message", "用户创建成功");
            result.put("user", savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "创建用户失败: " + e.getMessage());
            // 事务会自动回滚
        }
        
        return result;
    }

    // 删除用户
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查用户是否存在
            if (!userRepository.existsById(id)) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            // 删除用户
            userRepository.deleteById(id);

            result.put("success", true);
            result.put("message", "用户删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除用户失败: " + e.getMessage());
        }
        
        return result;
    }

    // 获取所有角色
    @GetMapping("/roles")
    public Iterable<Role> getRoles() {
        return roleRepository.findAll();
    }
}
