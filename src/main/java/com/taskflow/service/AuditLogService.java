package com.taskflow.service;

public interface AuditLogService {
    void log(String action, String entityType, Long entityId, String details, String performedBy);
}
