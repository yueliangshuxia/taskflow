package com.taskflow.service;

import com.taskflow.dto.TaskCreateDto;
import com.taskflow.dto.TaskDto;
import com.taskflow.entity.Task;
import com.taskflow.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    Page<TaskDto> findAccessibleTasks(String username, Pageable pageable);
    Page<TaskDto> findByProjectId(Long projectId, Pageable pageable);
    TaskDto findById(Long taskId);
    Task getTaskEntity(Long taskId);
    TaskDto createTask(Long projectId, TaskCreateDto createDto, String username);
    TaskDto updateTask(Long taskId, TaskCreateDto updateDto, String username);
    void deleteTask(Long taskId, String username);
    TaskDto updateStatus(Long taskId, TaskStatus newStatus, String username);
    List<TaskDto> findAllByProjectId(Long projectId);
}
