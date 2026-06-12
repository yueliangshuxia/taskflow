package com.taskflow.controller.rest;

import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskRestController {

    private final TaskService taskService;

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          Authentication auth) {
        try {
            TaskStatus newStatus = TaskStatus.valueOf(body.get("status"));
            var task = taskService.updateStatus(id, newStatus, auth.getName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", task.getStatus().name(),
                    "message", "状态更新成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
