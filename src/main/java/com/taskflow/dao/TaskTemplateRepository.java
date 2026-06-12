package com.taskflow.dao;

import com.taskflow.entity.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {
    List<TaskTemplate> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
