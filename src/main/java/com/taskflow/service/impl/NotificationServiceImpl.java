package com.taskflow.service.impl;

import com.taskflow.dao.NotificationRepository;
import com.taskflow.entity.Notification;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification createNotification(Long userId, String type, String message,
                                            String relatedEntityType, Long relatedEntityId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("通知不存在"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(Long userId, int limit) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }
}
