package com.taskflow.controller.rest;

import com.taskflow.dao.UserRepository;
import com.taskflow.entity.Notification;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping("/recent")
    public ResponseEntity<List<Notification>> getRecentNotifications(Authentication auth) {
        User user = getUser(auth);
        List<Notification> notifications = notificationService.getRecentNotifications(user.getId(), 10);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication auth) {
        User user = getUser(auth);
        long count = notificationService.getUnreadCount(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Authentication auth) {
        User user = getUser(auth);
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    private User getUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }
}
