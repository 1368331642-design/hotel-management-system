package com.hotel.repository;

import com.hotel.entity.ServiceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceLogRepository extends JpaRepository<ServiceLog, Long> {
}
