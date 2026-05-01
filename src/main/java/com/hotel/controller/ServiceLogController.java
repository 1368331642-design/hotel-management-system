package com.hotel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return serviceLogRepository.findAll(pageable);
    }

    // 获取已评价的服务日志（用户评价子模块）
    @GetMapping("/service-logs/reviewed")
    public Page<ServiceLog> getReviewedServiceLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return serviceLogRepository.findByRatingIsNotNull(pageable);
    }

    // 用户端 - 获取我的订单评价
    @GetMapping("/service-logs/my-reviews")
    public List<ServiceLog> getMyReviews(@RequestParam Long userId) {
        return serviceLogRepository.findByUserIdAndTypeOrderByCreateTimeDesc(userId, "订单评价");
    }

    // 创建服务日志（用户呼叫前台或反馈问题）
    @PostMapping("/service-logs")
    public ServiceLog createServiceLog(@RequestBody Map<String, Object> requestBody) {
        ServiceLog serviceLog = new ServiceLog();
        serviceLog.setType((String) requestBody.get("type"));
        serviceLog.setContent((String) requestBody.get("content"));
        serviceLog.setStatus((String) requestBody.get("status"));
        serviceLog.setCreateTime(new Date());
        
        Object ratingObj = requestBody.get("rating");
        if (ratingObj != null) {
            serviceLog.setRating(Integer.parseInt(ratingObj.toString()));
        }
        
        Object reviewObj = requestBody.get("review");
        if (reviewObj != null) {
            serviceLog.setReview(reviewObj.toString());
        }
        
        Object imagesObj = requestBody.get("images");
        if (imagesObj != null) {
            serviceLog.setImages(imagesObj.toString());
        }
        
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

    // 用户提交评价
    @PutMapping("/service-logs/{id}/review")
    public ServiceLog submitReview(@PathVariable Long id, @RequestBody Map<String, Object> requestBody) {
        ServiceLog existingLog = serviceLogRepository.findById(id).orElseThrow(() -> new RuntimeException("Service log not found"));
        
        // 获取评分
        Object ratingObj = requestBody.get("rating");
        if (ratingObj != null) {
            int rating = Integer.parseInt(ratingObj.toString());
            if (rating >= 1 && rating <= 5) {
                existingLog.setRating(rating);
            }
        }
        
        // 获取评价内容
        Object reviewObj = requestBody.get("review");
        if (reviewObj != null) {
            String review = reviewObj.toString();
            if (review.length() <= 500) {
                existingLog.setReview(review);
            }
        }
        
        Object imagesObj = requestBody.get("images");
        if (imagesObj != null) {
            existingLog.setImages(imagesObj.toString());
        }
        
        return serviceLogRepository.save(existingLog);
    }

    // 删除服务日志
    @DeleteMapping("/service-logs/{id}")
    public void deleteServiceLog(@PathVariable Long id) {
        serviceLogRepository.deleteById(id);
    }
}
