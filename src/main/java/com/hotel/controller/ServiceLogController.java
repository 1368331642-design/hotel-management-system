package com.hotel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.hotel.entity.ServiceLog;
import com.hotel.entity.User;
import com.hotel.repository.ServiceLogRepository;
import com.hotel.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class ServiceLogController {

    @Autowired
    private ServiceLogRepository serviceLogRepository;
    
    @Autowired
    private UserRepository userRepository;

    // 获取所有服务日志（支持分页）
    @GetMapping("/service-logs")
    public Page<ServiceLog> getServiceLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return serviceLogRepository.findAll(pageable);
    }

    // 创建服务日志（用户呼叫前台或反馈问题）
    @PostMapping("/service-logs")
    public ServiceLog createServiceLog(@RequestBody Map<String, Object> requestBody) {
        ServiceLog serviceLog = new ServiceLog();
        serviceLog.setType((String) requestBody.get("type"));
        serviceLog.setContent((String) requestBody.get("content"));
        serviceLog.setStatus((String) requestBody.get("status"));
        serviceLog.setCreateTime(new Date());
        
        // 设置用户
        Object userIdObj = requestBody.get("userId");
        if (userIdObj != null) {
            Long userId = Long.valueOf(userIdObj.toString());
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                serviceLog.setUser(user);
            }
        }
        
        return serviceLogRepository.save(serviceLog);
    }

    // 更新服务日志状态
    @PutMapping("/service-logs/{id}/status")
    public ServiceLog updateServiceLogStatus(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        ServiceLog existingLog = serviceLogRepository.findById(id).orElseThrow(() -> new RuntimeException("Service log not found"));
        existingLog.setStatus(requestBody.get("status"));
        return serviceLogRepository.save(existingLog);
    }

    // 删除服务日志
    @DeleteMapping("/service-logs/{id}")
    public void deleteServiceLog(@PathVariable Long id) {
        serviceLogRepository.deleteById(id);
    }
}
