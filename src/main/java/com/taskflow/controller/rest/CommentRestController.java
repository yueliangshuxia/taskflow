package com.taskflow.controller.rest;

import com.taskflow.dto.CommentDto;
import com.taskflow.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for task comments (AJAX).
 * Supports GET (list) and POST (create) operations.
 */
@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long taskId) {
        List<CommentDto> comments = commentService.getTaskComments(taskId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        CommentDto dto = commentService.addComment(taskId, content.trim(), auth.getName());
        return ResponseEntity.ok(dto);
    }
}
