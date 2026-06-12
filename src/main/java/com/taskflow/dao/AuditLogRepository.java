package com.taskflow.dao;

import com.taskflow.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    int deleteByPerformedAtBefore(LocalDateTime dateTime);
}
