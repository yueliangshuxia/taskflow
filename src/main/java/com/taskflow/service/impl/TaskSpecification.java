package com.taskflow.service.impl;

import com.taskflow.dto.TaskFilter;
import com.taskflow.entity.Task;
import com.taskflow.entity.enums.TaskPriority;
import com.taskflow.entity.enums.TaskStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public class TaskSpecification {

    public static Specification<Task> withFilter(TaskFilter filter) {
        return Specification
                .where(hasKeyword(filter.getKeyword()))
                .and(hasStatus(filter.getStatus()))
                .and(hasPriority(filter.getPriority()))
                .and(hasAssigneeId(filter.getAssigneeId()))
                .and(hasDueDateBefore(filter.getDueDateTo()))
                .and(hasDueDateAfter(filter.getDueDateFrom()))
                .and(hasProjectId(filter.getProjectId()));
    }

    public static Specification<Task> hasProjectId(Long projectId) {
        return (root, query, cb) -> {
            if (projectId == null) return null;
            return cb.equal(root.get("project").get("id"), projectId);
        };
    }

    private static Specification<Task> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.trim() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), cb.lower(cb.literal(pattern))),
                    cb.like(cb.lower(root.get("description")), cb.lower(cb.literal(pattern)))
            );
        };
    }

    private static Specification<Task> hasStatus(String status) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(status)) return null;
            try {
                return cb.equal(root.get("status"), TaskStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }

    private static Specification<Task> hasPriority(String priority) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(priority)) return null;
            try {
                return cb.equal(root.get("priority"), TaskPriority.valueOf(priority));
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }

    private static Specification<Task> hasAssigneeId(Long assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) return null;
            return cb.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    private static Specification<Task> hasDueDateBefore(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return null;
            return cb.lessThanOrEqualTo(root.get("dueDate"), date);
        };
    }

    private static Specification<Task> hasDueDateAfter(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return null;
            return cb.greaterThanOrEqualTo(root.get("dueDate"), date);
        };
    }
}
