package com.taskflow.service.impl;

import com.taskflow.entity.AuditLog;
import com.taskflow.dao.AuditLogRepository;
import com.taskflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String action, String entityType, Long entityId, String details, String performedBy) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .performedBy(performedBy)
                .build();
        auditLogRepository.save(log);
    }
}
