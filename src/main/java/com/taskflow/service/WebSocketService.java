package com.taskflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyUser(Long userId, String type, String message, Long relatedId) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                Map.of("type", type, "message", message, "relatedId", relatedId, "timestamp", System.currentTimeMillis())
        );
    }

    public void notifyProject(Long projectId, String type, String message) {
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId,
                Map.of("type", type, "message", message, "timestamp", System.currentTimeMillis())
        );
    }

    public void notifyTaskUpdate(Long taskId, String type, String message) {
        messagingTemplate.convertAndSend(
                "/topic/task/" + taskId,
                Map.of("type", type, "message", message, "timestamp", System.currentTimeMillis())
        );
    }
}
