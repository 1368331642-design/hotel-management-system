package com.hotel.repository;

import com.hotel.entity.ServiceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceLogRepository extends JpaRepository<ServiceLog, Long> {
    Page<ServiceLog> findByRatingIsNotNull(Pageable pageable);
    List<ServiceLog> findByUserIdAndTypeOrderByCreateTimeDesc(Long userId, String type);
}
