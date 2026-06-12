package com.taskflow.service;

import com.taskflow.dao.AuditLogRepository;
import com.taskflow.dao.VisitLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final AuditLogRepository auditLogRepository;
    private final VisitLogRepository visitLogRepository;

    /**
     * Clean audit logs older than 90 days, daily at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanOldAuditLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        int deleted = auditLogRepository.deleteByPerformedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Cleaned {} audit log entries older than {}", deleted, cutoff.toLocalDate());
        }
    }

    /**
     * Clean visit logs older than 30 days, daily at 3:30 AM
     */
    @Scheduled(cron = "0 30 3 * * ?")
    @Transactional
    public void cleanOldVisitLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deleted = visitLogRepository.deleteByVisitedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Cleaned {} visit log entries older than {}", deleted, cutoff.toLocalDate());
        }
    }

    /**
     * Log system health every hour
     */
    @Scheduled(fixedRate = 3600000)
    public void logSystemHealth() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        log.info("System health - Used: {}MB / Total: {}MB, Active threads: {}",
                usedMemory, totalMemory, Thread.activeCount());
    }
}
