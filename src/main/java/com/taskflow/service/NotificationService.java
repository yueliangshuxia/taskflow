package com.taskflow.service;

import com.taskflow.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification createNotification(Long userId, String type, String message,
                                     String relatedEntityType, Long relatedEntityId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    List<Notification> getRecentNotifications(Long userId, int limit);

    long getUnreadCount(Long userId);
}
