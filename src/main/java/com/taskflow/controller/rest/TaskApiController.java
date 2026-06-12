package com.taskflow.controller.rest;

import com.taskflow.dto.TaskCreateDto;
import com.taskflow.dto.TaskDto;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskApiController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<Page<TaskDto>> listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long projectId,
            Authentication auth) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (projectId != null) {
            return ResponseEntity.ok(taskService.findByProjectId(projectId, pageable));
        }
        return ResponseEntity.ok(taskService.findAccessibleTasks(auth.getName(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestParam Long projectId,
                                               @Valid @RequestBody TaskCreateDto dto,
                                               Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(projectId, dto, auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id,
                                               @Valid @RequestBody TaskCreateDto dto,
                                               Authentication auth) {
        return ResponseEntity.ok(taskService.updateTask(id, dto, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication auth) {
        taskService.deleteTask(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDto> updateStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body,
                                                 Authentication auth) {
        TaskStatus status = TaskStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(taskService.updateStatus(id, status, auth.getName()));
    }
}
